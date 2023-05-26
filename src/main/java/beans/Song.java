package beans;

public class Song {
	private String title;
	private String authorName;
	private String albumName;
	private String genre;
	private int albumYear;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getAlbumYear() {
		return this.albumYear;
	}

	public void setAlbumYear(int albumYear) {
		this.albumYear = albumYear;
	}
}