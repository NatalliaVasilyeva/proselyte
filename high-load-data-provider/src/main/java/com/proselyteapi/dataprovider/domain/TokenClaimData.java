package com.proselyteapi.dataprovider.domain;

import io.jsonwebtoken.Claims;

public record TokenClaimData(String token, Claims claims){}