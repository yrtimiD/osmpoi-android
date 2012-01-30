/**
 * 
 */
package il.yrtimid.osm.osmpoi.categories;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author yrtimid
 *
 */
public class Category implements Parcelable {
	public enum Type{
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
		this.query = source.readString();
		this.select = source.readString();
	}

	public List<Category> getSubCategories() {
		return subCategories;
	}
	
	public int getSubCategoriesCount(){
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
	
	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type.name());
		dest.writeTypedList(subCategories);
		dest.writeString(name);
		dest.writeString(query);
		dest.writeString(select);
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
