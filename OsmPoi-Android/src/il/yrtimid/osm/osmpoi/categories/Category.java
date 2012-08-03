/**
 * 
 */
package il.yrtimid.osm.osmpoi.categories;

import il.yrtimid.osm.osmpoi.searchparameters.*;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author yrtimid
 * 
 */
public class Category implements Parcelable {
	public enum Type {
		NONE,
		CUSTOM,
		STARRED,
		SEARCH,
		INLINE_SEARCH
	}

	private List<Category> subCategories = new ArrayList<Category>();
	private Type type;
	private String name;
	private String query;
	private String select;
	private Boolean localizable = false;
	private String icon;
	private Boolean subCategoriesFetched = false;
	private BaseSearchParameter searchParameter = null;

	public Category(Type type) {
		this.type = type;
	}

	/**
	 * @param source
	 */
	protected Category(Parcel source) {
		this.type = Enum.valueOf(Type.class, source.readString());
		source.readTypedList(subCategories, Category.CREATOR);
		this.name = source.readString();
		this.icon = source.readString();
		this.query = source.readString();
		this.select = source.readString();
		this.localizable = (Boolean) source.readValue(Boolean.class.getClassLoader());
		this.subCategoriesFetched = (Boolean) source.readValue(Boolean.class.getClassLoader());

		String className = source.readString();
		if (className.equals("NULL")){
			searchParameter = null;
		}else if (className.equals("SearchAround")) {
			searchParameter = source.readParcelable(SearchAround.class.getClassLoader());
		} else if (className.equals("SearchById")) {
			searchParameter = source.readParcelable(SearchById.class.getClassLoader());
		} else if (className.equals("SearchByKeyValue")) {
			searchParameter = source.readParcelable(SearchByKeyValue.class.getClassLoader());
		} else if (className.equals("SearchByParentId")) {
			searchParameter = source.readParcelable(SearchByParentId.class.getClassLoader());
		}
	}

	public List<Category> getSubCategories() {
		return subCategories;
	}

	public int getSubCategoriesCount() {
		return subCategories.size();
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public String getSelect() {
		return select;
	}

	public void setSelect(String select) {
		this.select = select;
	}

	public Boolean isLocalizable() {
		return localizable;
	}

	public void setLocalizable(Boolean isLocalizable) {
		this.localizable = isLocalizable;
	}

	public Boolean isSubCategoriesFetched() {
		return subCategoriesFetched;
	}

	public void setSubCategoriesFetched() {
		this.subCategoriesFetched = true;
	}

	public BaseSearchParameter getSearchParameter() {
		return searchParameter;
	}

	public void setSearchParameter(BaseSearchParameter searchParameter) {
		this.searchParameter = searchParameter;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type.name());
		dest.writeTypedList(subCategories);
		dest.writeString(name);
		dest.writeString(icon);
		dest.writeString(query);
		dest.writeString(select);
		dest.writeValue(localizable);
		dest.writeValue(subCategoriesFetched);
		if (searchParameter == null) {
			dest.writeString("NULL");
		} else {
			dest.writeString(searchParameter.getClass().getSimpleName());
			String className = searchParameter.getClass().getSimpleName();
			if (className.equals("SearchAround")) {
				dest.writeParcelable((SearchAround) searchParameter, flags);
			} else if (className.equals("SearchById")) {
				dest.writeParcelable((SearchById) searchParameter, flags);
			} else if (className.equals("SearchByKeyValue")) {
				dest.writeParcelable((SearchByKeyValue) searchParameter, flags);
			} else if (className.equals("SearchByParentId")) {
				dest.writeParcelable((SearchByParentId) searchParameter, flags);
			}
		}
	}

	public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {

		@Override
		public Category createFromParcel(Parcel source) {
			return new Category(source);
		}

		@Override
		public Category[] newArray(int size) {
			return new Category[size];
		}
	};
}
