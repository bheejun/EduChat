package org.eduai.educhat.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "cls_menu_auth")
class ClsMenuAuth (

    @Id
    @Column(name = "cls_id", nullable = false, length = Integer.MAX_VALUE)
    var clsId: String? = null,

    @Column(name = "component_list", length = Integer.MAX_VALUE)
    var componentList: String? = null

){
}