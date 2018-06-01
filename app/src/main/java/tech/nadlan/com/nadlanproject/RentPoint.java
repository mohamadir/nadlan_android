package tech.nadlan.com.nadlanproject;

/**
 * Created by מוחמד on 28/05/2018.
 */

public class RentPoint {
    private String type;
    private Double lat,lon;
    private String city,address,phone,ownerName,description;
    private int area,establishYear;
    private String photoPath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;


    public RentPoint() {
    }

    public RentPoint(String id,String type, Double lat, Double lon, String city, String address, String phone, String ownerName, String description, int area, int establishYear, String photoPath) {
        this.type = type;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.ownerName = ownerName;
        this.description = description;
        this.area = area;
        this.establishYear = establishYear;
        this.photoPath = photoPath;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getEstablishYear() {
        return establishYear;
    }

    public void setEstablishYear(int establishYear) {
        this.establishYear = establishYear;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
