package se.sundsvall.citizenassets.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status model", enumAsRef = true)
public enum Status {
    ACTIVE, EXPIRED, BLOCKED
}
