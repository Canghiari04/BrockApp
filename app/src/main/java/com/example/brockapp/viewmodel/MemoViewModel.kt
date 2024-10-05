package com.example.brockapp.viewmodel

import com.example.brockapp.database.BrockDB
import com.example.brockapp.extraObject.MyUser
import com.example.brockapp.database.MemoEntity

import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData

class MemoViewModel(private val db: BrockDB): ViewModel() {
    private val _memos = MutableLiveData<List<MemoEntity>>()
    val memos: LiveData<List<MemoEntity>> get() = _memos

    fun getMemos(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val memos = db.MemoDao().getMemoFromUsernameAndPeriod(MyUser.id, date)
            _memos.postValue(memos)
        }
    }

    fun insertMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.MemoDao().insertMemo(memo)
        }
    }

    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.MemoDao().deleteMemo(memo)
        }
    }
}