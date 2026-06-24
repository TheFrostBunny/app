package no.busswidget.widget

import no.busswidget.R

class SmallBussWidget : BaseBussWidget() {
    override val layoutId = R.layout.widget_small
    override val maxRows = 1
    override val rowViewIds = listOf(
        Triple(R.id.row1_line, R.id.row1_dest, R.id.row1_time)
    )
}
