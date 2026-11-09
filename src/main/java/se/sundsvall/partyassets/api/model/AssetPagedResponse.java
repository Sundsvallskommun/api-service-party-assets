package se.sundsvall.partyassets.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Schema(description = "Paged asset response")
public class AssetPagedResponse {

	@ArraySchema(schema = @Schema(implementation = Asset.class, accessMode = READ_ONLY))
	private List<Asset> assets;

	@JsonProperty("_meta")
	@Schema(implementation = PagingAndSortingMetaData.class, accessMode = READ_ONLY)
	private PagingAndSortingMetaData metaData;

	public static AssetPagedResponse create() {
		return new AssetPagedResponse();
	}

	public List<Asset> getAssets() {
		return assets;
	}

	public void setAssets(List<Asset> assets) {
		this.assets = assets;
	}

	public AssetPagedResponse withAssets(List<Asset> assets) {
		this.assets = assets;
		return this;
	}

	public PagingAndSortingMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(PagingAndSortingMetaData metaData) {
		this.metaData = metaData;
	}

	public AssetPagedResponse withMetaData(PagingAndSortingMetaData metaData) {
		this.metaData = metaData;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(assets, metaData);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AssetPagedResponse other = (AssetPagedResponse) obj;
		return Objects.equals(assets, other.assets) && Objects.equals(metaData, other.metaData);
	}

	@Override
	public String toString() {
		return "AssetPagedResponse [assets=" + assets + ", metaData=" + metaData + "]";
	}
}
