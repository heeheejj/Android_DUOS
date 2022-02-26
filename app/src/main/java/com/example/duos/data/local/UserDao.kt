package com.example.duos.data.local

import androidx.room.*
import com.example.duos.data.entities.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("SELECT * FROM UserTable WHERE userIdx = :userIdx") // 닉네임에 해당하는 user 가져오기
    fun getUser(userIdx: Int): User

    @Query("DELETE FROM UserTable") // 테이블에 들어있는 모든 값을 지워라
    fun clearAll()

    @Query("SELECT nickName FROM UserTable WHERE userIdx = :userIdx")
    fun getUserNickName(userIdx : Int): String

    @Query("SELECT profileImg FROM UserTable WHERE userIdx = :userIdx")
    fun getUserProfileImgUrl(userIdx: Int): String

//    @Query("UPDATE UserTable SET nickName = :nickName, location = :location, experience = :experience, introduce = :introduce")
//    fun updateProfile(
//    ): Completable
//    @Update(entity = User)
//    fun updateProfile(editProfile: EditProfile)
}



/*
 @Entity
 public class Playlist {
   @PrimaryKey(autoGenerate = true)
   long playlistId;
   String name;
   @ColumnInfo(defaultValue = "")
   String description
   @ColumnInfo(defaultValue = "normal")
   String category;
   @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
   String createdTime;
   @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
   String lastModifiedTime;
 }

 public class PlaylistCategory {
   long playlistId;
   String category;
   String lastModifiedTime
 }


}
 @Dao
 public interface PlaylistDao {
   @Update(entity = Playlist.class)
   public void updateCategory(PlaylistCategory... category);
 }

 */