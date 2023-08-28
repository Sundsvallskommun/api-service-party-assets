package se.sundsvall.citizenassets.integration.db.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.AUTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import se.sundsvall.citizenassets.api.model.Status;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "asset")
public class AssetEntity extends BaseEntity {

	@Id
	@UuidGenerator
	@GeneratedValue(strategy = AUTO)
	private String id;

	@Column(unique = true)
	private String assetId;

	private String partyId;

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
