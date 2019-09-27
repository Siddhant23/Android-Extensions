package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.text.SpanBuilder

/**
 * Fragment showing the use of a SpanBuilder
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpanbuilderFragment : AppBaseFragment(R.layout.fragment_spanbuilder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = true,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        val textView = view.findViewById<TextView>(R.id.text)
        val context = textView.context

        val text = SpanBuilder.of("This is a regular span")
                .prependSpace()
                .prepend(".")
                .prepend(1)
                .appendNewLine()
                .append(2)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a colored span")
                        .color(context, R.color.colorPrimaryDark)
                        .build())
                .appendNewLine()
                .append(3)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is an italicized span")
                        .italic()
                        .build())
                .appendNewLine()
                .append(4)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is an underlined span")
                        .underline()
                        .build())
                .appendNewLine()
                .append(5)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a bold span")
                        .bold()
                        .build())
                .appendNewLine()
                .append(6)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a resized span")
                        .resize(1.2f)
                        .build())
                .appendNewLine()
                .append(7)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a clickable span")
                        .click(textView,
                                { paint -> paint.isUnderlineText = true },
                                { uiState = uiState.copy(snackbarText = "Clicked text!") })
                        .build())
                .appendNewLine()
                .build()

        textView.text = text
    }

    companion object {
        fun newInstance(): SpanbuilderFragment = SpanbuilderFragment().apply { arguments = Bundle() }
    }
}