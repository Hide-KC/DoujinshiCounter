package com.kcpoipoi.doujinshicounter;

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.android.synthetic.main.fragment_detected.*

class DetectedBooksFragment : Fragment() {
    companion object {
        fun newInstance(targetFragment: Fragment?): DetectedBooksFragment {
            val fragment = DetectedBooksFragment()
            fragment.setTargetFragment(targetFragment, 0)
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_detected, container, false)
        return view
    }

    fun setDetectedItems(sparseArray: SparseArray<Barcode>){
        val listView = view?.findViewById<ListView>(R.id.detected_list)
        if (listView != null){
            val adapter = if (listView.adapter == null){
                DetectedBooksAdapter(context!!)
            } else {
                listView.adapter as DetectedBooksAdapter
            }
            adapter.clear()

            for (key_i in 0 until sparseArray.size()){
                val item = sparseArray.get(sparseArray.keyAt(key_i)).rawValue
                val book: Book? = TempBookList.getBook(Integer.parseInt(item))
                if (book != null){
                    adapter.add(DetectedBook(book))
                }
            }
            listView.adapter = adapter
        }
    }

    fun clear(){
        val listView = view?.findViewById<ListView>(R.id.detected_list)
        if (listView != null) {
            val adapter = if (listView.adapter == null) {
                DetectedBooksAdapter(context!!)
            } else {
                listView.adapter as DetectedBooksAdapter
            }
            adapter.clear()
        }
    }
}