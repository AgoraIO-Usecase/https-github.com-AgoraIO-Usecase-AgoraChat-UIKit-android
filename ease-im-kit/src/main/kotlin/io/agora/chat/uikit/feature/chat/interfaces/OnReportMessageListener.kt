package io.agora.chat.uikit.feature.chat.interfaces


interface OnReportMessageListener {
    /**
     * modify message success
     */
    fun onReportMessageSuccess(msgId:String){}

    /**
     * modify message failure
     * @param msgId
     * @param code
     * @param error
     */
    fun onReportMessageFailure(msgId: String?, code: Int, error: String?){}
}