package lv.bis.fpkdv.utilit

import android.content.Context
import lv.bis.fpkdv.R

class Translate(val context: Context) {
    public fun getTranslatedString(stringID:Int):String{
        val index = context.getSharedPreferences(R.string.PreferenceName.toString(), Context.MODE_PRIVATE).getInt(
            R.string.LanguageID.toString(),0)
        val result = context.getResources().getStringArray(stringID)[index]
        return result
    }
}