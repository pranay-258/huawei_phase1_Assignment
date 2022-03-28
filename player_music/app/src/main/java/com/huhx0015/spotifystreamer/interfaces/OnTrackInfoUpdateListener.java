package com.huhx0015.spotifystreamer.interfaces;

/**
 * -------------------------------------------------------------------------------------------------
 * [OnTrackInfoUpdateListener] INTERFACE
 * PROGRAMMER: Michael Yoon Huh (Huh X0015)
 * DESCRIPTION: This is an interface class that is used as a signalling conduit between the
 * SSMainActivity class and the SSPlayerFragment class, which contains methods for updating the
 * activity on the current track that is playing in the background.
 * -------------------------------------------------------------------------------------------------
 */
public interface OnTrackInfoUpdateListener {

    // setCurrentTrack(): Interface method which signals the attached activity to update the current
    // Bitmap, track name, and Spotify track URL.
    void setCurrentTrack(String songName, String trackUrl, int position);

    // updateActionBar(): Interface method which signals the attached activity to update the
    // ActionBar with the current selected track song name.
    void updateActionBar(String songName);
}
