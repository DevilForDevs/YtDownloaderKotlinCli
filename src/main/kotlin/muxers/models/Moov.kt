package muxers.models

import muxers.models.moov.Mvhd

class Moov : ContainerBox("moov") {


    fun addMovieHeader(
        mvhd: Mvhd
    ) {
        addBox(mvhd)
    }


    /*fun addTrack(
        trak: Trak
    ) {
        addBox(trak)
    }*/
}