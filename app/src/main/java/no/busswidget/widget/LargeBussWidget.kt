package no.busswidget.widget

import no.busswidget.R

class LargeBussWidget : BaseBussWidget() {
    override val layoutId = R.layout.widget_large
    override val maxRows = 4
    override val rowViewIds = listOf(
        Triple(R.id.row1_line, R.id.row1_dest, R.id.row1_time),
        Triple(R.id.row2_line, R.id.row2_dest, R.id.row2_time),
        Triple(R.id.row3_line, R.id.row3_dest, R.id.row3_time),
        Triple(R.id.row4_line, R.id.row4_dest, R.id.row4_time)
    )
}
