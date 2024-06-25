package se.sundsvall.partyassets.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import se.sundsvall.partyassets.api.model.Status;
import se.sundsvall.partyassets.integration.db.model.StatusEntity;

public final class StatusMapper {

	private StatusMapper() {
		// Private constructor to prevent instantiation
	}

	public static Map<Status, List<String>> toReasons(final List<StatusEntity> entities) {
		return ofNullable(entities).orElse(emptyList()).stream()
			.map(entity -> Map.entry(Status.valueOf(entity.getName()), entity.getReasons()))
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static StatusEntity toEntity(final Status status, final List<String> reasons, final String municipalityId) {
		return StatusEntity.create()
			.withName(status.name())
			.withReasons(retreiveUniqueItems(reasons))
			.withMunicipalityId(municipalityId);
	}

	private static List<String> retreiveUniqueItems(final List<String> list) {
		return new ArrayList<>(ofNullable(list).orElse(emptyList())
			.stream()
			.map(StringUtils::stripToEmpty)
			.map(StringUtils::upperCase)
			.distinct()
			.toList());
	}

}
