package com.mojang.authlib.yggdrasil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.authlib.yggdrasil.response.Response;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.mojang.util.UUIDTypeAdapter;
import kml.Constants;
import kml.Utils;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class YggdrasilMinecraftSessionService extends HttpMinecraftSessionService {
    private static final String[] WHITELISTED_DOMAINS = new String[]{".minecraft.net", ".mojang.com"};
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";
    private static final URL JOIN_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/join");
    private static final URL CHECK_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");
    private final Gson gson = (new GsonBuilder()).registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private final LoadingCache<GameProfile, GameProfile> insecureProfiles;

    protected YggdrasilMinecraftSessionService(YggdrasilAuthenticationService authenticationService) {
        super(authenticationService);
        this.insecureProfiles = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build(new CacheLoader<GameProfile, GameProfile>() {
            public GameProfile load(GameProfile key) throws Exception {
                return YggdrasilMinecraftSessionService.this.fillGameProfile(key, false);
            }
        });
    }

    public void joinServer(GameProfile profile, String authenticationToken, String serverId) throws AuthenticationException {
        JoinMinecraftServerRequest request = new JoinMinecraftServerRequest();
        request.accessToken = authenticationToken;
        request.selectedProfile = profile.getId();
        request.serverId = serverId;
        this.getAuthenticationService().makeRequest(JOIN_URL, request, Response.class);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException {
        return this.hasJoinedServer(user, serverId, null);
    }

    public GameProfile hasJoinedServer(GameProfile user, String serverId, InetAddress address) throws AuthenticationUnavailableException {
        HashMap arguments = new HashMap();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        if(address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        URL url = HttpAuthenticationService.concatenateURL(CHECK_URL, HttpAuthenticationService.buildQuery(arguments));

        try {
            HasJoinedMinecraftServerResponse ignored = (HasJoinedMinecraftServerResponse)this.getAuthenticationService().makeRequest(url, (Object)null, HasJoinedMinecraftServerResponse.class);
            if(ignored != null && ignored.getId() != null) {
                GameProfile result = new GameProfile(ignored.getId(), user.getName());
                if(ignored.getProperties() != null) {
                    result.getProperties().putAll(ignored.getProperties());
                }

                return result;
            } else {
                return null;
            }
        } catch (AuthenticationUnavailableException var8) {
            throw var8;
        } catch (AuthenticationException var9) {
            return null;
        }
    }

    public Map<Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        Property textureProperty = (Property)Iterables.getFirst(profile.getProperties().get("textures"), (Object)null);
        if(textureProperty == null) {
            try {
                JSONArray users = new JSONArray();
                users.put(profile.getName());
                byte[] data = users.toString().getBytes();
                String response = Utils.sendPost(Constants.GET_PROFILESID, data, new HashMap<String, String>());
                JSONArray rdata = new JSONArray(response);
                if (rdata.length() == 1){
                    JSONObject user = rdata.getJSONObject(0);
                    if (user.has("id")){
                        String profileID = user.getString("id");
                        JSONObject profileData = new JSONObject(Utils.readURL(new URL("http://mc.krothium.com/profiles/" + profileID)));
                        if (profileData.has("properties")){
                            JSONArray properties = profileData.getJSONArray("properties");
                            if (properties.length() == 1){
                                JSONObject property = properties.getJSONObject(0);
                                if (property.has("name") && property.has("value")){
                                    if (property.getString("name").equals("textures") && !property.getString("value").isEmpty()){
                                        String textures = new String(Base64.decodeBase64(property.getString("value")), Charsets.UTF_8);
                                        MinecraftTexturesPayload result = this.gson.fromJson(textures, MinecraftTexturesPayload.class);
                                        return result.getTextures();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex){
                LOGGER.error("Failed to fetch data from profile " + profile.getId() + " with name " + profile.getName());
            }
            return new HashMap();
        } else {
            MinecraftTexturesPayload result;
            try {
                String e = new String(Base64.decodeBase64(textureProperty.getValue()), Charsets.UTF_8);
                result = this.gson.fromJson(e, MinecraftTexturesPayload.class);
            } catch (JsonParseException var7) {
                LOGGER.error("Could not decode textures payload", var7);
                return new HashMap();
            }

            if(result.getTextures() == null) {
                return new HashMap();
            } else {
                return result.getTextures();
            }
        }
    }

    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        return profile.getId() == null?profile:(!requireSecure?(GameProfile)this.insecureProfiles.getUnchecked(profile):this.fillGameProfile(profile, true));
    }

    protected GameProfile fillGameProfile(GameProfile profile, boolean requireSecure) {
        try {
            URL e = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + profile.getId().toString().replaceAll("-", ""));
            e = HttpAuthenticationService.concatenateURL(e, "unsigned=" + !requireSecure);
            MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse)this.getAuthenticationService().makeRequest(e, (Object)null, MinecraftProfilePropertiesResponse.class);
            if(response == null) {
                LOGGER.debug("Couldn\'t fetch profile properties for " + profile + " as the profile does not exist");
                return profile;
            } else {
                GameProfile result = new GameProfile(response.getId(), response.getName());
                result.getProperties().putAll(response.getProperties());
                profile.getProperties().putAll(response.getProperties());
                LOGGER.debug("Successfully fetched profile properties for " + profile);
                return result;
            }
        } catch (AuthenticationException var6) {
            LOGGER.warn("Couldn\'t look up profile properties for " + profile, var6);
            return profile;
        }
    }

    public YggdrasilAuthenticationService getAuthenticationService() {
        return (YggdrasilAuthenticationService)super.getAuthenticationService();
    }
}
