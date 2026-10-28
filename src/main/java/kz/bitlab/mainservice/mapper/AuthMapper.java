package kz.bitlab.mainservice.mapper;

import kz.bitlab.mainservice.dto.TokenResponse;
import kz.bitlab.mainservice.keycloak.dto.KeycloakTokenResponse;
import org.mapstruct.Mapper;

@Mapper
public interface AuthMapper {

    TokenResponse toTokenResponse(KeycloakTokenResponse token);
}
