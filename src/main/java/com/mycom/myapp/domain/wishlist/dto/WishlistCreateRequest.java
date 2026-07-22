package com.mycom.myapp.domain.wishlist.dto;

import jakarta.validation.constraints.NotNull;

public record WishlistCreateRequest(@NotNull Long roomId) {
}
