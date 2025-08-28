package se.sundsvall.partyassets.service;

public final class Constants {

	private Constants() {}

	public static final String MESSAGE_JSON_SCHEMA_NOT_FOUND = "A JsonSchema with ID '%s' was not found!";
	public static final String MESSAGE_JSON_SCHEMA_ALREADY_EXISTS = "A JsonSchema already exists with ID '%s'!";
	public static final String MESSAGE_JSON_SCHEMA_WITH_GREATER_VERSION_ALREADY_EXISTS = "A JsonSchema with a greater version already exists! (see schema with ID: '%s')";
	public static final String MESSAGE_JSON_SCHEMA_NOT_ABLE_TO_DELETE_REFERENCED_SCHEMAS = "The JsonSchema has %s referencing assets! Deletion of schemas with references is not possible!";
}
