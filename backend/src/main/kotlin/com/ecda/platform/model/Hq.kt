package com.ecda.platform.model

import jakarta.persistence.*

@Entity
@Table(name = "hqs")
data class Hq(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val code: String = "",

    @Column(nullable = false)
    val name: String = ""
)
