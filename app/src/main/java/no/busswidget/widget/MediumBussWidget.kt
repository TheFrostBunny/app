package no.busswidget.widget

import no.busswidget.R

class MediumBussWidget : BaseBussWidget() {
    override val layoutId = R.layout.widget_medium
    override val maxRows = 2
    override val rowViewIds = listOf(
        Triple(R.id.row1_line, R.id.row1_dest, R.id.row1_time),
        Triple(R.id.row2_line, R.id.row2_dest, R.id.row2_time)
    )
}
