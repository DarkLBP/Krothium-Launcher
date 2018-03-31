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
import com.mojang.util.UUIDTypeAdapter;
import kml.utils.Utils;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class YggdrasilMinecraftSessionService extends HttpMinecraftSessionService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";
    private static final URL JOIN_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/join");
    private static final URL CHECK_URL = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/hasJoined");
    private final Gson gson = (new GsonBuilder()).registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private final LoadingCache<GameProfile, GameProfile> insecureProfiles;
    private final HashMap<String, Map<Type, MinecraftProfileTexture>> cache = new HashMap<>();
    private static final String GET_PROFILESID = "https://mc.krothium.com/api/profiles/minecraft";
    private static final String GET_PROFILESID_MOJANG = "https://api.mojang.com/profiles/minecraft";


    protected YggdrasilMinecraftSessionService(YggdrasilAuthenticationService authenticationService) {
        super(authenticationService);
        this.insecureProfiles = CacheBuilder.newBuilder().expireAfterWrite(6L, TimeUnit.HOURS).build(new CacheLoader<GameProfile, GameProfile>() {
            public GameProfile load(GameProfile key) throws Exception {
                return YggdrasilMinecraftSessionService.this.fillGameProfile(key, false);
            }
        });
    }

    private static boolean isWhitelistedDomain(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException ignored) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'");
        }
        return true;
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
        Map<String, Object> arguments = new HashMap();
        arguments.put("username", user.getName());
        arguments.put("serverId", serverId);
        if (address != null) {
            arguments.put("ip", address.getHostAddress());
        }

        URL url = HttpAuthenticationService.concatenateURL(CHECK_URL, HttpAuthenticationService.buildQuery(arguments));

        try {
            HasJoinedMinecraftServerResponse response = (HasJoinedMinecraftServerResponse)this.getAuthenticationService().makeRequest(url, (Object)null, HasJoinedMinecraftServerResponse.class);
            if (response != null && response.getId() != null) {
                GameProfile result = new GameProfile(response.getId(), user.getName());
                if (response.getProperties() != null) {
                    result.getProperties().putAll(response.getProperties());
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
        if (textureProperty == null) {
            return fetchCustomTextures(profile, requireSecure);
        } else {
            MinecraftTexturesPayload result;
            try {
                String json = new String(Base64.decodeBase64(textureProperty.getValue()), Charsets.UTF_8);
                result = (MinecraftTexturesPayload)this.gson.fromJson(json, MinecraftTexturesPayload.class);
            } catch (JsonParseException var7) {
                LOGGER.error("Could not decode textures payload", var7);
                return new HashMap();
            }

            if (result != null && result.getTextures() != null) {
                Iterator var8 = result.getTextures().entrySet().iterator();

                Map.Entry entry;
                do {
                    if (!var8.hasNext()) {
                        return result.getTextures();
                    }

                    entry = (Map.Entry)var8.next();
                } while(isWhitelistedDomain(((MinecraftProfileTexture)entry.getValue()).getUrl()));

                LOGGER.error("Textures payload has been tampered with (non-whitelisted domain)");
                return new HashMap();
            } else {
                return new HashMap();
            }
        }
    }

    public Map<Type, MinecraftProfileTexture> fetchCustomTextures(GameProfile profile, boolean requireSecure) {
        if (cache.containsKey(profile.getName())) {
            System.out.println("Serving cached textures for: " + profile.getName() + " / " + profile.getId());
            return cache.get(profile.getName());
        } else {
            try {
                System.out.println("Serving textures for: " + profile.getName() + " / " + profile.getId());
                JSONArray users = new JSONArray();
                users.put(profile.getName());
                byte[] data = users.toString().getBytes();
                String profileID = null;
                String response = Utils.sendPost(GET_PROFILESID, data, new HashMap<String, String>());
                JSONArray rdata = new JSONArray(response);
                if (rdata.length() == 1) {
                    JSONObject user = rdata.getJSONObject(0);
                    if (user.has("id")) {
                        profileID = user.getString("id");
                        System.out.println("Found user " + profile.getName() + " on Krothium server.");
                    }
                } else {
                    System.out.println("No textures found on Krothium for " + profile.getName() + ". Searching in Mojang server...");
                    HashMap<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    response = Utils.sendPost(GET_PROFILESID_MOJANG, data, params);
                    rdata = new JSONArray(response);
                    if (rdata.length() == 1) {
                        JSONObject user = rdata.getJSONObject(0);
                        if (user.has("id")) {
                            profileID = user.getString("id");
                            System.out.println("Found user " + profile.getName() + " on Mojang server.");
                        }
                    }
                }
                if (profileID != null) {
                    JSONObject profileData = new JSONObject(Utils.readURL("https://sessionserver.mojang.com/session/minecraft/profile/" + profileID + "?unsigned=" + !requireSecure));
                    if (profileData.has("properties")) {
                        JSONArray properties = profileData.getJSONArray("properties");
                        if (properties.length() == 1) {
                            JSONObject property = properties.getJSONObject(0);
                            if (property.has("name") && property.has("value")) {
                                if (property.getString("name").equals("textures") && !property.getString("value").isEmpty()) {
                                    String textures = new String(Base64.decodeBase64(property.getString("value")), Charsets.UTF_8);
                                    MinecraftTexturesPayload result = this.gson.fromJson(textures, MinecraftTexturesPayload.class);
                                    cache.put(profile.getName(), result.getTextures());
                                    System.out.println("Found textures for " + profile.getName() + ".");
                                    return result.getTextures();
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Failed to fetch data from profile " + profile.getId() + " with name " + profile.getName());
            }
        }
        System.out.println("No textures found for " + profile.getName());
        return new HashMap();
    }

    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        if (profile.getId() == null) {
            return profile;
        } else {
            return !requireSecure ? (GameProfile)this.insecureProfiles.getUnchecked(profile) : this.fillGameProfile(profile, true);
        }
    }

    protected GameProfile fillGameProfile(GameProfile profile, boolean requireSecure) {
        try {
            URL url = HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(profile.getId()));
            url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);
            MinecraftProfilePropertiesResponse response = (MinecraftProfilePropertiesResponse)this.getAuthenticationService().makeRequest(url, (Object)null, MinecraftProfilePropertiesResponse.class);
            if (response == null) {
                LOGGER.debug("Couldn't fetch profile properties for " + profile + " as the profile does not exist");
                return profile;
            } else {
                GameProfile result = new GameProfile(response.getId(), response.getName());
                result.getProperties().putAll(response.getProperties());
                profile.getProperties().putAll(response.getProperties());
                LOGGER.debug("Successfully fetched profile properties for " + profile);
                return result;
            }
        } catch (AuthenticationException var6) {
            LOGGER.warn("Couldn't look up profile properties for " + profile, var6);
            return profile;
        }
    }

    public YggdrasilAuthenticationService getAuthenticationService() {
        return (YggdrasilAuthenticationService) super.getAuthenticationService();
    }
}
