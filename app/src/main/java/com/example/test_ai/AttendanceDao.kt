package com.example.test_ai

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE name = :groupName")
    suspend fun deleteGroup(groupName: String)

    @Query("SELECT * FROM students WHERE groupName = :groupName")
    fun getStudentsByGroup(groupName: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: Int)

    @Query("DELETE FROM students WHERE groupName = :groupName")
    suspend fun deleteStudentsByGroup(groupName: String)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)
}
