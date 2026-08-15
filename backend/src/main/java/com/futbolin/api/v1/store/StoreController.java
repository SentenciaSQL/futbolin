package com.futbolin.api.v1.store;

import com.futbolin.core.exception.ApiException;
import com.futbolin.core.exception.ErrorCode;
import com.futbolin.core.security.UserPrincipal;
import com.futbolin.data.entity.CosmeticEntity;
import com.futbolin.data.entity.UserCosmeticEntity;
import com.futbolin.data.entity.UserProfileEntity;
import com.futbolin.data.repository.CosmeticRepository;
import com.futbolin.data.repository.UserCosmeticRepository;
import com.futbolin.data.repository.UserProfileRepository;
import com.futbolin.data.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final CosmeticRepository cosmetics;
    private final UserCosmeticRepository userCosmetics;
    private final UserProfileRepository profiles;
    private final UserRepository users;

    public StoreController(
            CosmeticRepository cosmetics,
            UserCosmeticRepository userCosmetics,
            UserProfileRepository profiles,
            UserRepository users
    ) {
        this.cosmetics = cosmetics;
        this.userCosmetics = userCosmetics;
        this.profiles = profiles;
        this.users = users;
    }

    @GetMapping("/cosmetics")
    public Object list() {
        return cosmetics.findByActiveTrue();
    }

    @GetMapping("/me")
    public Object mine(@AuthenticationPrincipal UserPrincipal principal) {
        return userCosmetics.findByUserId(principal.id());
    }

    @PostMapping("/cosmetics/{id}/buy")
    public Map<String, Object> buy(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        CosmeticEntity cosmetic = cosmetics.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (userCosmetics.existsByUserIdAndCosmeticId(principal.id(), id)) {
            throw new ApiException(ErrorCode.CONFLICT, "Already owned");
        }
        UserProfileEntity profile = profiles.findById(principal.id()).orElseThrow();
        if (profile.getLevel() < cosmetic.getMinLevel()) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Level too low");
        }
        if (profile.getCoins() < cosmetic.getPriceCoins()) {
            throw new ApiException(ErrorCode.INSUFFICIENT_COINS);
        }
        profile.setCoins(profile.getCoins() - cosmetic.getPriceCoins());
        UserCosmeticEntity owned = new UserCosmeticEntity();
        owned.setUser(users.getReferenceById(principal.id()));
        owned.setCosmetic(cosmetic);
        userCosmetics.save(owned);
        return Map.of("owned", true, "coins", profile.getCoins());
    }

    @PostMapping("/cosmetics/{id}/equip")
    public void equip(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        UserCosmeticEntity owned = userCosmetics.findByUserIdAndCosmeticId(principal.id(), id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        CosmeticEntity cosmetic = owned.getCosmetic();
        UserProfileEntity profile = profiles.findById(principal.id()).orElseThrow();
        switch (cosmetic.getType()) {
            case "AVATAR" -> profile.setAvatarKey(cosmetic.getKey());
            case "FRAME" -> profile.setFrameKey(cosmetic.getKey());
            case "TITLE" -> profile.setTitleKey(cosmetic.getKey());
            default -> {
            }
        }
        owned.setEquipped(true);
    }
}
