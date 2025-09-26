package se.sundsvall.partyassets.service;

public final class Constants {

	static final String MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_ID = "A JsonSchema with ID '%s' was not found!";
	static final String MESSAGE_JSON_SCHEMA_NOT_FOUND_BY_NAME = "A JsonSchema with name '%s' was not found!";
	static final String JSON_SCHEMA_ALREADY_EXISTS = "A JsonSchema already exists with ID '%s'!";
	static final String JSON_SCHEMA_WITH_GREATER_VERSION_EXISTS = "A JsonSchema with a greater version already exists! (see schema with ID: '%s')";
	static final String JSON_SCHEMA_REFERENCED_ASSETS = "The JsonSchema has %s referencing assets! Deletion of schemas with references is not possible!";

	private Constants() {}
}
