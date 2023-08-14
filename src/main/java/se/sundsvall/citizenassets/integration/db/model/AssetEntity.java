package se.sundsvall.citizenassets.integration.db.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.AUTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.citizenassets.api.model.Status;

@Entity
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "asset")
public class AssetEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = AUTO)
	private UUID id;

	@Column(unique = true)
	private String assetId;

	private UUID partyId;

	@ElementCollection
	private List<String> caseReferenceIds;

	private String type;

	private LocalDate issued;

	private LocalDate validTo;

	@Enumerated(STRING)
	private Status status;

	private String description;

	@ElementCollection
	private Map<String, String> additionalParameters;
}
