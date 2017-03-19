package com.mojang.authlib.yggdrasil;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import kml.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class YggdrasilGameProfileRepository implements GameProfileRepository {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASE_URL = "https://api.mojang.com/";
    private static final String SEARCH_PAGE_URL = "https://api.mojang.com/profiles/";
    private static final int ENTRIES_PER_PAGE = 2;
    private static final int MAX_FAIL_COUNT = 3;
    private static final int DELAY_BETWEEN_PAGES = 100;
    private static final int DELAY_BETWEEN_FAILURES = 750;
    private final YggdrasilAuthenticationService authenticationService;

    public YggdrasilGameProfileRepository(YggdrasilAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void findProfilesByNames(String[] names, Agent agent, ProfileLookupCallback callback) {
        HashSet criteria = Sets.newHashSet();
        String[] page = names;
        int i$ = names.length;

        for (int var19 = 0; var19 < i$; ++var19) {
            String var20 = page[var19];
            if (!Strings.isNullOrEmpty(var20)) {
                criteria.add(var20.toLowerCase());
            }
        }

        byte var211 = 0;
        Iterator var221 = Iterables.partition(criteria, ENTRIES_PER_PAGE).iterator();

        while (var221.hasNext()) {
            List var21 = (List) var221.next();
            int var22 = 0;

            while (true) {
                boolean failed = false;

                try {
                    ProfileSearchResultsResponse var18 = (ProfileSearchResultsResponse) this.authenticationService.makeRequest(Constants.GET_PROFILESID, var21, ProfileSearchResultsResponse.class);
                    var22 = 0;
                    LOGGER.debug("Page {} returned {} results, parsing", new Object[]{Integer.valueOf(var211), Integer.valueOf(var18.getProfiles().length)});
                    HashSet var23 = Sets.newHashSet(var21);
                    GameProfile[] var24 = var18.getProfiles();
                    int var25 = var24.length;
                    for (int var26 = 0; var26 < var25; ++var26) {
                        GameProfile var27 = var24[var26];
                        System.out.println("Profile data from " + var27.getName() + " got from the Krothium API");
                        LOGGER.debug("Successfully looked up profile {}", new Object[]{var27});
                        var23.remove(var27.getName().toLowerCase());
                        callback.onProfileLookupSucceeded(var27);
                    }

                    Iterator var261 = Iterables.partition(var23, ENTRIES_PER_PAGE).iterator();
                    while (var261.hasNext()) {
                        List var30 = (List) var261.next();
                        var18 = (ProfileSearchResultsResponse) this.authenticationService.makeRequest(Constants.GET_PROFILESID_MOJANG, var30, ProfileSearchResultsResponse.class);
                        var22 = 0;
                        LOGGER.debug("Page {} returned {} results, parsing", new Object[]{Integer.valueOf(var211), Integer.valueOf(var18.getProfiles().length)});
                        var23 = Sets.newHashSet(var21);
                        var24 = var18.getProfiles();
                        var25 = var24.length;
                        for (int var26 = 0; var26 < var25; ++var26) {
                            GameProfile var27 = var24[var26];
                            System.out.println("Profile data from " + var27.getName() + " got from the Mojang API");
                            LOGGER.debug("Successfully looked up profile {}", new Object[]{var27});
                            var23.remove(var27.getName().toLowerCase());
                            callback.onProfileLookupSucceeded(var27);
                        }
                        var261 = var23.iterator();
                        while (var261.hasNext()) {
                            String var271 = (String) var261.next();
                            LOGGER.debug("Couldn\'t find profile {}", new Object[]{var271});
                            callback.onProfileLookupFailed(new GameProfile(null, var271), new ProfileNotFoundException("Server did not find the requested profile"));
                        }
                        try {
                            Thread.sleep(DELAY_BETWEEN_PAGES);
                        } catch (InterruptedException var191) {
                            ;
                        }

                    }
                    try {
                        Thread.sleep(DELAY_BETWEEN_PAGES);
                    } catch (InterruptedException var191) {
                        ;
                    }

                } catch (AuthenticationException var201) {
                    AuthenticationException e = var201;
                    ++var22;
                    if (var22 == MAX_FAIL_COUNT) {
                        Iterator ignored = var21.iterator();

                        while (ignored.hasNext()) {
                            String name = (String) ignored.next();
                            LOGGER.debug("Couldn\'t find profile {} because of a server error", new Object[]{name});
                            callback.onProfileLookupFailed(new GameProfile(null, name), e);
                        }
                    } else {
                        try {
                            Thread.sleep(DELAY_BETWEEN_FAILURES);
                        } catch (InterruptedException var181) {
                            ;
                        }

                        failed = true;
                    }
                }

                if (!failed) {
                    break;
                }
            }
        }

    }
}
