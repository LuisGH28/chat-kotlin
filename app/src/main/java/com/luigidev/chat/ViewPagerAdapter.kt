package com.luigidev.chat

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.luigidev.chat.Fragmentos.FragmentChats
import com.luigidev.chat.Fragmentos.FragmentPerfil
import com.luigidev.chat.Fragmentos.FragmentUsuarios

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        FragmentPerfil(),
        FragmentUsuarios(),
        FragmentChats()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
