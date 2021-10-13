package com.example.stegnography;
public class ImageUploadInfo {
	
	public String imageName;
	
	public String imageURL;
	
	public String Name;
	
	public ImageUploadInfo() {
	
	}
	
	public ImageUploadInfo(String name,String fileName, String url) {
		
		this.Name = name;
		this.imageName=fileName;
		this.imageURL= url;
	}
	
	public String getImageName() { return imageName; }
	
	public String getImageURL() {
		return imageURL;
	}
	
}
