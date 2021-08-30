package com.sixsixsix.dy.model

import java.io.Serializable

/**
 * @author : jiaBing
 * @date   : 2021/8/30
 * @desc   :
 */
data class ResultBean(val list: List<ItemResult>) : Serializable
data class ItemResult(val url: String, val type: Type) : Serializable
enum class Type {
    Video,
    Img
}