package com.eosphor.nonameradio.station

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
// Временно комментируем импорты material-popup-menu
// import com.github.zawadz88.materialpopupmenu.MaterialPopupMenu
// import com.github.zawadz88.materialpopupmenu.popupMenu
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.sizeDp
import com.eosphor.nonameradio.R
import com.eosphor.nonameradio.RadioDroidApp
import com.eosphor.nonameradio.Utils
import com.eosphor.nonameradio.players.PlayStationTask
import com.eosphor.nonameradio.players.selector.PlayerType

object StationPopupMenu {
    // Временная заглушка для совместимости
    fun open(view: View, context: Context, activity: FragmentActivity, station: DataRadioStation, itemAdapterStation: ItemAdapterStation): Any? {
        // TODO: Заменить на Material3 компоненты или PopupMenu
        return null
    }
}