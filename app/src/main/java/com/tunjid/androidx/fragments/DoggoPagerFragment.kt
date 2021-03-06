package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoPagerAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.constraintlayout.animator.ViewPagerIndicatorAnimator
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewmodels.DoggoViewModel
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DoggoPagerFragment : AppBaseFragment(R.layout.fragment_doggo_pager),
        Navigator.TransactionModifier {

    override val insetFlags: InsetFlags = InsetFlags.NONE

    private val viewModel by viewModels<DoggoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getColors(Color.TRANSPARENT).observe(this) { view?.setBackgroundColor(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarShows = false,
                toolBarMenu = 0,
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                showsBottomNav = false,
                lightStatusBar = false,
                navBarColor = Color.TRANSPARENT,
                fabClickListener = View.OnClickListener { Doggo.transitionDoggo?.let { navigator.push(AdoptDoggoFragment.newInstance(it)) } }
        )

        val viewPager = view.findViewById<ViewPager>(R.id.view_pager)
        val resources = resources
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        viewPager.adapter = DoggoPagerAdapter(viewModel.doggos, childFragmentManager)
        viewPager.currentItem = Doggo.transitionIndex
        viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) = onDoggoSwiped(position)
        })

        val indicatorAnimator = ViewPagerIndicatorAnimator(
                indicatorWidth = indicatorSize,
                indicatorHeight = indicatorSize,
                indicatorPadding = resources.getDimensionPixelSize(R.dimen.half_margin),
                activeDrawable = R.drawable.ic_doggo_24dp,
                inActiveDrawable = R.drawable.ic_circle_24dp,
                guide = view.findViewById(R.id.guide),
                container = view as ConstraintLayout,
                viewPager = viewPager
        )

        indicatorAnimator.addIndicatorWatcher { indicator, position, fraction, _ ->
            val radians = Math.PI * fraction
            val sine = (-sin(radians)).toFloat()
            val cosine = cos(radians).toFloat()
            val maxScale = max(abs(cosine), 0.4f)

            val currentIndicator = indicatorAnimator.getIndicatorAt(position)
            currentIndicator.scaleX = maxScale
            currentIndicator.scaleY = maxScale
            indicator.translationY = indicatorSize * sine
        }

        indicatorAnimator.addIndicatorWatcher watcher@{ indicator, position, fraction, _ ->
            if (fraction == 0F) return@watcher

            val static = intArrayOf(0, 0).apply { indicatorAnimator.getIndicatorAt(position).getLocationInWindow(this) }
            val dynamic = intArrayOf(0, 0).apply { indicator.getLocationInWindow(this) }
            val toTheRight = dynamic[0] > static[0]

            viewModel.onSwiped(position, fraction, toTheRight)
        }

        onDoggoSwiped(viewPager.currentItem)
        prepareSharedElementTransition()

        if (savedInstanceState == null) postponeEnterTransition()
    }

    private fun onDoggoSwiped(position: Int) {
        Doggo.doggos[position].apply {
            Doggo.transitionDoggo = this
            uiState = uiState.copy(fabText = getString(R.string.adopt_doggo, name.replace(" ", "")))
        }
    }

    @SuppressLint("CommitTransaction")
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val root = view ?: return
        val doggo = Doggo.transitionDoggo ?: return
        val childRoot = root.findViewWithTag<View>(doggo) ?: return
        val imageView = childRoot.findViewById<ImageView>(R.id.doggo_image) ?: return

        transaction
                .setReorderingAllowed(true)
                .addSharedElement(imageView, imageView.hashTransitionName(doggo))
    }

    private fun prepareSharedElementTransition() {
        sharedElementEnterTransition = baseSharedTransition()
        sharedElementReturnTransition = baseSharedTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                val viewPager = view?.findViewById<ViewPager>(R.id.view_pager) ?: return
                if (names == null || sharedElements == null || view == null) return

                val currentFragment = Objects.requireNonNull<PagerAdapter>(viewPager.adapter)
                        .instantiateItem(viewPager, Doggo.transitionIndex) as Fragment
                val view = currentFragment.view ?: return

                sharedElements[names[0]] = view.findViewById(R.id.doggo_image)
            }
        })
    }

    companion object {
        fun newInstance(): DoggoPagerFragment = DoggoPagerFragment().apply { arguments = Bundle(); prepareSharedElementTransition() }
    }

}
