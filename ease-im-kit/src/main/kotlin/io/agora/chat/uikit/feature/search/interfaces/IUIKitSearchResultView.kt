package io.agora.chat.uikit.feature.search.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView

interface IUIKitSearchResultView: IControlDataView {
    fun searchSuccess(result: Any)
    fun searchBlockUserSuccess(result: Any){}
}