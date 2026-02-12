package com.example.appdevwardrobeinf246;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiService {

    public static class RegisterRequest {
        private String username;
        private String password;

        public RegisterRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ResetPasswordRequest {
        private String username;
        private String new_password;

        public ResetPasswordRequest(String username, String newPassword) {
            this.username = username;
            this.new_password = newPassword;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getNew_password() { return new_password; }
        public void setNew_password(String new_password) { this.new_password = new_password; }
    }

    public static class DeleteAccountRequest {
        private String username;
        private String password;

        public DeleteAccountRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class DeleteAccountAdminRequest {
        private String username;

        public DeleteAccountAdminRequest(String username) {
            this.username = username;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class GetClothesRequest {
        private int user_id;

        public GetClothesRequest(int user_id) {
            this.user_id = user_id;
        }

        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    // NEW: Get single item request
    public static class GetItemRequest {
        public int id;
        public int user_id;

        public GetItemRequest(int id, int user_id) {
            this.id = id;
            this.user_id = user_id;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    // NEW: Get single item response
    public static class GetItemResponse {
        private String status;
        private String message;
        private clothitem item;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public clothitem getItem() { return item; }
        public void setItem(clothitem item) { this.item = item; }
    }

    public static class AddClothRequest {
        private int user_id;
        private String name;
        private String type;
        private String area;
        private String description;
        private String image_uri;

        public AddClothRequest(int user_id, String name, String type, String area,
                               String description, String image_uri) {
            this.user_id = user_id;
            this.name = name;
            this.type = type;
            this.area = area;
            this.description = description;
            this.image_uri = image_uri;
        }

        public AddClothRequest() {} // Default constructor for flexibility

        // Getters and setters
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImage_uri() { return image_uri; }
        public void setImage_uri(String image_uri) { this.image_uri = image_uri; }
    }

    public static class UpdateClothRequest {
        public int id;
        public int user_id;
        public String name;
        public String type;
        public String area;
        public String description;
        public String image_uri;

        public UpdateClothRequest() {} // Default constructor

        public UpdateClothRequest(int id, int user_id, String name, String type, String area,
                                  String description, String image_uri) {
            this.id = id;
            this.user_id = user_id;
            this.name = name;
            this.type = type;
            this.area = area;
            this.description = description;
            this.image_uri = image_uri;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImage_uri() { return image_uri; }
        public void setImage_uri(String image_uri) { this.image_uri = image_uri; }
    }

    public static class DeleteClothRequest {
        public int id;
        public int user_id;

        public DeleteClothRequest() {} // Default constructor

        public DeleteClothRequest(int id, int user_id) {
            this.id = id;
            this.user_id = user_id;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    public static class ApiResponse {
        private String status;
        private String message;
        private UserData user;
        private Integer user_id;
        private List<clothitem> clothes;
        private Integer item_id;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public UserData getUser() { return user; }
        public void setUser(UserData user) { this.user = user; }

        public Integer getUser_id() { return user_id; }
        public void setUser_id(Integer user_id) { this.user_id = user_id; }

        public List<clothitem> getClothes() { return clothes; }
        public void setClothes(List<clothitem> clothes) { this.clothes = clothes; }

        public Integer getItem_id() { return item_id; }
        public void setItem_id(Integer item_id) { this.item_id = item_id; }
    }

    public static class UserData {
        private int id;
        private String username;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class GetOutfitsRequest {
        private int user_id;
        public GetOutfitsRequest(int user_id) { this.user_id = user_id; }
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    public static class OutfitSummary {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OutfitSummary that = (OutfitSummary) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
        private int id;
        private String name;
        private String description;
        private int times_worn;
        private long last_worn_timestamp;
        private String created_at;
        private String updated_at;
        private List<clothitem> clothing_items;   // full items for preview

        // Getters and setters (required for Gson)
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getTimes_worn() { return times_worn; }
        public void setTimes_worn(int times_worn) { this.times_worn = times_worn; }
        public long getLast_worn_timestamp() { return last_worn_timestamp; }
        public void setLast_worn_timestamp(long last_worn_timestamp) { this.last_worn_timestamp = last_worn_timestamp; }
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        public String getUpdated_at() { return updated_at; }
        public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
        public List<clothitem> getClothing_items() { return clothing_items; }
        public void setClothing_items(List<clothitem> clothing_items) { this.clothing_items = clothing_items; }
    }

    public static class GetOutfitsResponse {
        private String status;
        private String message;
        private List<OutfitSummary> outfits;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<OutfitSummary> getOutfits() { return outfits; }
        public void setOutfits(List<OutfitSummary> outfits) { this.outfits = outfits; }
    }

    // Get single outfit (full detail)
    public static class GetOutfitRequest {
        private int outfit_id;
        private int user_id;
        public GetOutfitRequest(int outfit_id, int user_id) { this.outfit_id = outfit_id; this.user_id = user_id; }
        public int getOutfit_id() { return outfit_id; }
        public void setOutfit_id(int outfit_id) { this.outfit_id = outfit_id; }
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    public static class GetOutfitResponse {
        private String status;
        private String message;
        private OutfitDetail outfit;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public OutfitDetail getOutfit() { return outfit; }
        public void setOutfit(OutfitDetail outfit) { this.outfit = outfit; }
    }

    public static class OutfitDetail {
        private int id;
        private String name;
        private String description;
        private int times_worn;
        private Long last_worn_timestamp;
        private String created_at;
        private String updated_at;
        private List<clothitem> clothing_items;

        // Getters and setters (same as OutfitSummary)
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getTimes_worn() { return times_worn; }
        public void setTimes_worn(int times_worn) { this.times_worn = times_worn; }
        public Long getLast_worn_timestamp() { return last_worn_timestamp; }
        public void setLast_worn_timestamp(Long last_worn_timestamp) { this.last_worn_timestamp = last_worn_timestamp; }
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        public String getUpdated_at() { return updated_at; }
        public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
        public List<clothitem> getClothing_items() { return clothing_items; }
        public void setClothing_items(List<clothitem> clothing_items) { this.clothing_items = clothing_items; }
    }

    // Add outfit
    public static class AddOutfitRequest {
        private int user_id;
        private String name;
        private String description;
        private List<Integer> clothing_ids;

        public AddOutfitRequest(int user_id, String name, String description, List<Integer> clothing_ids) {
            this.user_id = user_id;
            this.name = name;
            this.description = description;
            this.clothing_ids = clothing_ids;
        }
        // Getters and setters...
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<Integer> getClothing_ids() { return clothing_ids; }
        public void setClothing_ids(List<Integer> clothing_ids) { this.clothing_ids = clothing_ids; }
    }

    // Update outfit
    public static class UpdateOutfitRequest {
        private int outfit_id;
        private int user_id;
        private String name;          // optional
        private String description;   // optional
        private List<Integer> clothing_ids; // optional

        public UpdateOutfitRequest(int outfit_id, int user_id) {
            this.outfit_id = outfit_id;
            this.user_id = user_id;
        }
        public int getOutfit_id() { return outfit_id; }
        public void setOutfit_id(int outfit_id) { this.outfit_id = outfit_id; }
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<Integer> getClothing_ids() { return clothing_ids; }
        public void setClothing_ids(List<Integer> clothing_ids) { this.clothing_ids = clothing_ids; }
    }

    public static class DeleteOutfitRequest {
        private int outfit_id;
        private int user_id;
        public DeleteOutfitRequest(int outfit_id, int user_id) { this.outfit_id = outfit_id; this.user_id = user_id; }
        public int getOutfit_id() { return outfit_id; }
        public void setOutfit_id(int outfit_id) { this.outfit_id = outfit_id; }
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    public static class WearOutfitRequest {
        private int outfit_id;
        private int user_id;
        public WearOutfitRequest(int outfit_id, int user_id) { this.outfit_id = outfit_id; this.user_id = user_id; }
        public int getOutfit_id() { return outfit_id; }
        public void setOutfit_id(int outfit_id) { this.outfit_id = outfit_id; }
        public int getUser_id() { return user_id; }
        public void setUser_id(int user_id) { this.user_id = user_id; }
    }

    public static class SimpleResponse {
        private String status;
        private String message;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    // ---------- WASH TRACKER API DTOs ----------

    // Request: get all schedules for a user
    public static class GetWashSchedulesRequest {
        @SerializedName("user_id")
        private int userId;
        public GetWashSchedulesRequest(int userId) { this.userId = userId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
    }

    // Summary of a wash schedule (for the list view) – fields public for easy access
    public static class WashScheduleSummary {
        @SerializedName("id") public int serverId;
        @SerializedName("name") public String name;
        @SerializedName("max_wears_before_wash") public int maxWearsBeforeWash;
        @SerializedName("next_wash_date") public Long nextWashDate;
        @SerializedName("is_recurring") public boolean isRecurring;
        @SerializedName("recurrence_days") public int recurrenceDays;
        @SerializedName("notifications_enabled") public boolean notificationsEnabled;
        @SerializedName("last_notification_sent") public long lastNotificationSent;
        @SerializedName("created_at") public String createdAt;
        @SerializedName("updated_at") public String updatedAt;
        @SerializedName("items") public List<clothitem> items;
        @SerializedName("outfits") public List<OutfitSummary> outfits;
    }

    public static class GetWashSchedulesResponse {
        @SerializedName("status") private String status;
        @SerializedName("message") private String message;
        @SerializedName("schedules") private List<WashScheduleSummary> schedules;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<WashScheduleSummary> getSchedules() { return schedules; }
        public void setSchedules(List<WashScheduleSummary> schedules) { this.schedules = schedules; }
    }

    // Request: get a single schedule (detail view)
    public static class GetWashScheduleRequest {
        @SerializedName("schedule_id") private int scheduleId;
        @SerializedName("user_id") private int userId;
        public GetWashScheduleRequest(int scheduleId, int userId) {
            this.scheduleId = scheduleId;
            this.userId = userId;
        }
        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
    }

    // Full detail of a schedule (reuse summary)
    public static class WashScheduleDetail extends WashScheduleSummary {}

    public static class GetWashScheduleResponse {
        @SerializedName("status") private String status;
        @SerializedName("message") private String message;
        @SerializedName("schedule") private WashScheduleDetail schedule;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public WashScheduleDetail getSchedule() { return schedule; }
        public void setSchedule(WashScheduleDetail schedule) { this.schedule = schedule; }
    }

    // Request: create a new wash schedule
    public static class AddWashScheduleRequest {
        @SerializedName("user_id") private int userId;
        @SerializedName("name") private String name;
        @SerializedName("max_wears_before_wash") private int maxWearsBeforeWash;
        @SerializedName("next_wash_date") private Long nextWashDate;
        @SerializedName("is_recurring") private int isRecurring; // 0/1
        @SerializedName("recurrence_days") private int recurrenceDays;
        @SerializedName("notifications_enabled") private int notificationsEnabled;
        @SerializedName("item_ids") private List<Integer> itemIds;
        @SerializedName("outfit_ids") private List<Integer> outfitIds;

        public AddWashScheduleRequest(int userId, String name, int maxWearsBeforeWash) {
            this.userId = userId;
            this.name = name;
            this.maxWearsBeforeWash = maxWearsBeforeWash;
            this.isRecurring = 0;
            this.recurrenceDays = 7;
            this.notificationsEnabled = 1;
            this.itemIds = new ArrayList<>();
            this.outfitIds = new ArrayList<>();
        }

        // Getters and setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getMaxWearsBeforeWash() { return maxWearsBeforeWash; }
        public void setMaxWearsBeforeWash(int maxWearsBeforeWash) { this.maxWearsBeforeWash = maxWearsBeforeWash; }
        public Long getNextWashDate() { return nextWashDate; }
        public void setNextWashDate(Long nextWashDate) { this.nextWashDate = nextWashDate; }
        public int getIsRecurring() { return isRecurring; }
        public void setIsRecurring(int isRecurring) { this.isRecurring = isRecurring; }
        public int getRecurrenceDays() { return recurrenceDays; }
        public void setRecurrenceDays(int recurrenceDays) { this.recurrenceDays = recurrenceDays; }
        public int getNotificationsEnabled() { return notificationsEnabled; }
        public void setNotificationsEnabled(int notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
        public List<Integer> getItemIds() { return itemIds; }
        public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
        public List<Integer> getOutfitIds() { return outfitIds; }
        public void setOutfitIds(List<Integer> outfitIds) { this.outfitIds = outfitIds; }
    }

    public static class UpdateWashScheduleRequest {
        @SerializedName("schedule_id") private int scheduleId;
        @SerializedName("user_id") private int userId;
        @SerializedName("name") private String name;               // optional
        @SerializedName("max_wears_before_wash") private Integer maxWearsBeforeWash; // optional
        @SerializedName("next_wash_date") private Long nextWashDate; // optional
        @SerializedName("is_recurring") private Integer isRecurring; // optional
        @SerializedName("recurrence_days") private Integer recurrenceDays; // optional
        @SerializedName("notifications_enabled") private Integer notificationsEnabled; // optional
        @SerializedName("item_ids") private List<Integer> itemIds;    // optional – if provided, REPLACES all items
        @SerializedName("outfit_ids") private List<Integer> outfitIds; // optional – if provided, REPLACES all outfits

        public UpdateWashScheduleRequest(int scheduleId, int userId) {
            this.scheduleId = scheduleId;
            this.userId = userId;
        }

        // Getters and setters
        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getMaxWearsBeforeWash() { return maxWearsBeforeWash; }
        public void setMaxWearsBeforeWash(Integer maxWearsBeforeWash) { this.maxWearsBeforeWash = maxWearsBeforeWash; }
        public Long getNextWashDate() { return nextWashDate; }
        public void setNextWashDate(Long nextWashDate) { this.nextWashDate = nextWashDate; }
        public Integer getIsRecurring() { return isRecurring; }
        public void setIsRecurring(Integer isRecurring) { this.isRecurring = isRecurring; }
        public Integer getRecurrenceDays() { return recurrenceDays; }
        public void setRecurrenceDays(Integer recurrenceDays) { this.recurrenceDays = recurrenceDays; }
        public Integer getNotificationsEnabled() { return notificationsEnabled; }
        public void setNotificationsEnabled(Integer notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
        public List<Integer> getItemIds() { return itemIds; }
        public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
        public List<Integer> getOutfitIds() { return outfitIds; }
        public void setOutfitIds(List<Integer> outfitIds) { this.outfitIds = outfitIds; }
    }

    // Request: delete a wash schedule
    public static class DeleteWashScheduleRequest {
        @SerializedName("schedule_id") private int scheduleId;
        @SerializedName("user_id") private int userId;
        public DeleteWashScheduleRequest(int scheduleId, int userId) {
            this.scheduleId = scheduleId;
            this.userId = userId;
        }
        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
    }

    // Request: mark a schedule as washed
    public static class MarkWashScheduleRequest {
        @SerializedName("schedule_id") private int scheduleId;
        @SerializedName("user_id") private int userId;
        public MarkWashScheduleRequest(int scheduleId, int userId) {
            this.scheduleId = scheduleId;
            this.userId = userId;
        }
        public int getScheduleId() { return scheduleId; }
        public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
    }
    public interface ApiInterface {
        @POST("register.php")
        Call<ApiResponse> register(@Body RegisterRequest request);

        @POST("login.php")
        Call<ApiResponse> login(@Body LoginRequest request);

        @POST("resetpassword.php")
        Call<ApiResponse> resetPassword(@Body ResetPasswordRequest request);

        @POST("deleteaccount.php")
        Call<ApiResponse> deleteAccount(@Body DeleteAccountRequest request);

        @POST("getclothes.php")
        Call<ApiResponse> getClothes(@Body GetClothesRequest request);

        @POST("listoutfits.php")
        Call<GetOutfitsResponse> getOutfits(@Body GetOutfitsRequest request);

        @POST("getoutfit.php")
        Call<GetOutfitResponse> getOutfit(@Body GetOutfitRequest request);

        @POST("addoutfit.php")
        Call<SimpleResponse> addOutfit(@Body AddOutfitRequest request);

        @POST("updateoutfit.php")
        Call<SimpleResponse> updateOutfit(@Body UpdateOutfitRequest request);

        @POST("deleteoutfit.php")
        Call<SimpleResponse> deleteOutfit(@Body DeleteOutfitRequest request);

        @POST("wearoutfit.php")
        Call<SimpleResponse> wearOutfit(@Body WearOutfitRequest request);
        @POST("getitem.php")
        Call<GetItemResponse> getItem(@Body GetItemRequest request);

        @POST("addcloth.php")
        Call<ApiResponse> addCloth(@Body AddClothRequest request);

        @POST("updatecloth.php")
        Call<ApiResponse> updateCloth(@Body UpdateClothRequest request);

        @POST("deletecloth.php")
        Call<ApiResponse> deleteCloth(@Body DeleteClothRequest request);

        @POST("getwashschedules.php")
        Call<GetWashSchedulesResponse> getWashSchedules(@Body GetWashSchedulesRequest request);

        @POST("getwashschedule.php")
        Call<GetWashScheduleResponse> getWashSchedule(@Body GetWashScheduleRequest request);

        @POST("addwashschedule.php")
        Call<SimpleResponse> addWashSchedule(@Body AddWashScheduleRequest request);

        @POST("updatewashschedule.php")
        Call<SimpleResponse> updateWashSchedule(@Body UpdateWashScheduleRequest request);

        @POST("deletewashschedule.php")
        Call<SimpleResponse> deleteWashSchedule(@Body DeleteWashScheduleRequest request);

        @POST("markwashschedule.php")
        Call<SimpleResponse> markWashSchedule(@Body MarkWashScheduleRequest request);
    }
}