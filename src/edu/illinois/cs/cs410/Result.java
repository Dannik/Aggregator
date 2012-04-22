package edu.illinois.cs.cs410;

//Each page, returned to the browser
public class Result {
	private String id, title, date, description, image, link, contents;

	public Result(String id, String title, String date, String description, String image,
			String link, String contents) {
		super();
		this.id = id;
		this.title = title;
		this.date = date;
		this.description = description;
		this.image = image;
		this.link = link;
		this.contents = contents;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
}