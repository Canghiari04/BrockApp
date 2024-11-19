package com.example.brockapp.viewModel

import com.example.brockapp.room.BrockDB
import com.example.brockapp.room.MemosEntity
import com.example.brockapp.extraObject.MyUser

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class MemoViewModel(private val db: BrockDB): ViewModel() {

    private val _memos = MutableLiveData<List<MemosEntity>>()
    val memos: LiveData<List<MemosEntity>> get() = _memos

    fun getMemos(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val memos = db.MemosDao().getMemosByUsernameAndPeriod(MyUser.username, date)
            _memos.postValue(memos)
        }
    }

    fun insertMemo(memo: MemosEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.MemosDao().insertMemo(memo)
        }
    }

    fun updateMemo(id: Long, title: String, description: String, activityType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.MemosDao().updateMemo(id, title, description, activityType)
        }
    }

    fun deleteMemo(memo: MemosEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.MemosDao().deleteMemo(memo)
        }
    }
}