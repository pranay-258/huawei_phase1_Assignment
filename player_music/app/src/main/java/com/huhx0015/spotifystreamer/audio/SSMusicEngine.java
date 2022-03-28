package com.huhx0015.spotifystreamer.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.huhx0015.spotifystreamer.interfaces.OnMusicPlayerListener;
import java.io.IOException;

/** -----------------------------------------------------------------------------------------------
 *  [SSMusicEngine] CLASS
 *  PROGRAMMER: Michael Yoon Huh (Huh X0015)
 *  DESCRIPTION: SSMusicEngine class is used to support music playback for the application.
 *  Code adapted from my own HuhX Game Sound Engine project here:
 *  https://github.com/huhx0015/HuhX_Game_Sound_Engine
 *  -----------------------------------------------------------------------------------------------
 */

public class SSMusicEngine {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // AUDIO VARIABLES:
    private MediaPlayer backgroundSong; // MediaPlayer variable for background song.
    private String currentSong; // Used for determining what song is playing in the background.
    private Boolean isPaused; // Used for determining if a song has been paused.
    public int songPosition; // Used for resuming playback on a song that was paused.
    public Boolean musicOn; // Used for determining whether music is playing in the background.

    // FRAGMENT VARIABLES:
    private Fragment playerFragment; // References the SSPlayerFragment for updating the music player interface.

    // LOGGING VARIABLES:
    private static final String LOG_TAG = SSMusicEngine.class.getSimpleName(); // Used for logging output to logcat.

    // SYSTEM VARIABLES:
    private Context context; // References the application context.

    /** INITIALIZATION FUNCTIONALITY ___________________________________________________________ **/

    // SSMusicEngine(): Constructor for SSMusicEngine class.
    private final static SSMusicEngine ss_music = new SSMusicEngine();

    // SSMusicEngine(): Deconstructor for SSMusicEngine class.
    private SSMusicEngine() {}

    // getInstance(): Returns the ss_sounds instance.
    public static SSMusicEngine getInstance() { return ss_music; }

    // initializeAudio(): Initializes the SSMusicEngine class variables.
    public void initializeAudio(Context con) {

        Log.d(LOG_TAG, "INITIALIZING: Initializing music engine.");

        this.context = con; // Sets the application Context reference.
        this.backgroundSong = new MediaPlayer(); // Instantiates the main MediaPlayer object.
        this.isPaused = true; // Indicates that the song is not paused by default.
        this.musicOn = true; // Indicates that music playback is enabled by default.
        this.currentSong = "STOPPED"; // Sets the "STOPPED" condition for the song name string.
        this.songPosition = 0; // Sets the song position to the beginning of the song by default.

        Log.d(LOG_TAG, "INITIALIZING: Music engine initialization complete.");
    }

    // attachFragment(): Attaches the player fragment to this class.
    public void attachFragment(Fragment fragment) {
        this.playerFragment = fragment; // Sets the player fragment to interact with.
        Log.d(LOG_TAG, "attachFragment: Fragment attached.");
    }

    /** MUSIC FUNCTIONALITY ____________________________________________________________________ **/

    // getSongDuration(): Returns the current song's maximum duration.
    public int getSongDuration() {

        int maxDuration = 0;

        if ( (backgroundSong != null) && (backgroundSong.isPlaying()) ) {
            maxDuration = backgroundSong.getDuration() / 1000; // Retrieves the maximum song duration.
        }

        return maxDuration;
    }

    // getSongPosition(): Returns the current song position of the current song playing in the
    // background.
    public int getSongPosition() {

        int position = 0; // Used to reference the current song position.

        if (backgroundSong != null) {

            // Returns the current song position as long as the song is currently playing.
            if (backgroundSong.isPlaying()) {
                position = backgroundSong.getCurrentPosition() / 1000;
            }

            // If the song has stopped playing, a value of -1 is returned to indicate that the song
            // is no longer playing.
            else {
                return -1;
            }
        }

        return position;
    }

    // setSongPosition(): Sets the song position of the current song playing in the background.
    public void setSongPosition(int position) {

        Log.d(LOG_TAG, "setSongPosition(): Updating song position at: " + position);

        if (backgroundSong != null) {

            songPosition = position * 1000; // Sets the new song position.

            // Sets the song position as long as the song is currently playing in the background.
            if (backgroundSong.isPlaying()) {
                backgroundSong.seekTo(songPosition);
            }
        }
    }

    // playSongUrl(): Plays the music file based on the specified song URL.
    // Set loop variable to true to enable infinite song looping.
    // TRUE: Loops the song infinitely.
    // FALSE: Disables song looping.
    public void playSongUrl(String songUrl, Boolean loop) {

        // If the music option has been enabled, a song is selected based on the passed in songUrl string.
        if (musicOn) {

            // Calls playSong to create a MediaPlayer object and play the song.
            Log.d(LOG_TAG, "PREPARING: Preparing song for playback.");
            playSong(songUrl, loop);
        }

        // Outputs a message to logcat indicating that the song cannot be played.
        else {
            Log.d(LOG_TAG, "ERROR: Song cannot be played. Music engine is currently disabled.");
        }
    }

    //  playSong(): Sets up a MediaPlayer object and begins playing the song.
    private void playSong(final String songUrl, boolean loop) {

        Boolean isSongReady; // Used to determine if song is ready for playback.

        // Checks to see if the MediaPlayer class has been instantiated first before playing a song.
        // This is to prevent a rare null pointer exception bug.
        if (backgroundSong == null) {

            Log.d(LOG_TAG, "WARNING: MediaPlayer object was null. Re-initializing MediaPlayer object.");
            backgroundSong = new MediaPlayer();
        }

        else {

            // Stops any songs currently playing in the background.
            if (backgroundSong.isPlaying()) {
                Log.d(LOG_TAG, "PREPARING: Song currently playing in the background. Stopping playback before switching to a new song.");
                backgroundSong.stop();
            }

            // Sets up the MediaPlayer object for the song to be played.
            releaseMedia(); // Releases MediaPool resources.
            backgroundSong = new MediaPlayer(); // Initializes the MediaPlayer.
            backgroundSong.setWakeMode(context.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // Sets the wake lock mode.
            backgroundSong.setAudioStreamType(AudioManager.STREAM_MUSIC); // Sets the audio type for the MediaPlayer object.

            Log.d(LOG_TAG, "PREPARING: MediaPlayer stream type set to STREAM_MUSIC.");

            // Attempts to set the data source for the MediaPlayer object.
            try {
                backgroundSong.setDataSource(songUrl);
                isSongReady = true;
            }

            // IO exception handler.
            catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "ERROR: playSong(): I/O exception occurred.");
                return;
            }

            // Null pointer exception handlers.
            catch (NullPointerException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "ERROR: playSong(): Null pointer exception occurred.");
                return;
            }

            if (isSongReady) {

                // Prepares the song track for playback.
                backgroundSong.prepareAsync(); // Prepares the stream asynchronously.
                backgroundSong.setLooping(loop); // Enables infinite looping of music.

                Log.d(LOG_TAG, "PREPARING: Loop condition has been set to " + loop + ".");

                // Sets up the listener for the MediaPlayer object. Song playback begins immediately
                // once the MediaPlayer object is ready.
                backgroundSong.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                        // If the song was previously paused, resume the song at it's previous location.
                        if (isPaused) {

                            Log.d(LOG_TAG, "PREPARING: Song was previously paused, resuming song playback.");

                            mediaPlayer.seekTo(songPosition); // Jumps to the position where the song left off.
                            songPosition = 0; // Resets songPosition variable after song's position has been set.
                            isPaused = false; // Indicates that the song is no longer paused.
                        }

                        Log.d(LOG_TAG, "MUSIC: Song playback has begun.");

                        mediaPlayer.start(); // Begins playing the song.
                        playbackStatus(true); // Updates SSPlayerFragment on the song playback status.
                        setDuration(mediaPlayer.getDuration() / 1000); // Retrieves the maximum song duration.
                    }
                });
            }
        }
    }

    // isSongPlaying(): Determines if a song is currently playing in the background.
    public Boolean isSongPlaying() {
        return backgroundSong.isPlaying();
    }

    // pauseSong(): Pauses any songs playing in the background and returns it's position.
    public void pauseSong() {

        Log.d(LOG_TAG, "MUSIC: Music playback has been paused.");

        // Checks to see if mapSong has been initialized first before saving the song position and pausing the song.
        if (backgroundSong != null) {

            songPosition = backgroundSong.getCurrentPosition(); // Retrieves the current song position and saves it.

            // Pauses the song only if there is a song is currently playing.
            if (backgroundSong.isPlaying()) {
                backgroundSong.pause(); // Pauses the song.
            }

            playbackStatus(false); // Updates SSPlayerFragment on the song playback status.

            isPaused = true; // Indicates that the song is currently paused.
            currentSong = "PAUSED";
        }
    }

    //  stopSong(): Stops any songs playing in the background.
    public void stopSong() {

        // Checks to see if mapSong has been initiated first before stopping song playback.
        if ( (backgroundSong != null) && (musicOn) ) {
            backgroundSong.stop(); // Stops any songs currently playing in the background.
            playbackStatus(false); // Updates SSPlayerFragment on the song playback status.
            currentSong = "STOPPED";
            Log.d(LOG_TAG, "MUSIC: Song playback has been stopped.");
        }

        else {
            Log.d(LOG_TAG, "ERROR: Cannot stop song, as MediaPlayer object is already null.");
        }
    }

    // releaseMedia(): Used to release the resources being used by mediaPlayer objects.
    public void releaseMedia() {

        // Releases MediaPool resources.
        if (backgroundSong != null) {

            backgroundSong.reset();
            backgroundSong.release();
            backgroundSong = null;

            Log.d(LOG_TAG, "RELEASE: MediaPlayer object has been released.");
        }

        else {
            Log.d(LOG_TAG, "ERROR: MediaPlayer object is null and cannot be released.");
        }
    }

    // updatePlayer(): Updates the attached player fragment on the playback status, as well as the
    // max duration of the song.
    public void updatePlayer() {

        // If a song is currently playing in the background, the attached playerFragment is signaled
        // on the current song playback status.
        if (isSongPlaying()) {
            playbackStatus(true);
        }

        // Signals to the playerFragment that no song is currently playing in the background.
        else {
            playbackStatus(false);
        }

        int maxDuration = getSongDuration(); // Retrieves the current song's maximum duration.

        // Sends the updated duration value to the playerFragment.
        if (maxDuration != 0) {
            setDuration(maxDuration);
        }
    }

    /** INTERFACE METHODS ______________________________________________________________________ **/

    // playbackStatus(): Signals the SSPlayerFragment on the current playback status of the
    // streaming Spotify song.
    private void playbackStatus(Boolean isPlay) {

        if (playerFragment != null) {

            Log.d(LOG_TAG, "playbackStatus(): Attempting to update the SSPlayerFragment of the song playback status.");

            try { ((OnMusicPlayerListener) playerFragment).playbackStatus(isPlay); }
            catch (ClassCastException cce) {} // Catch for class cast exception errors.
        }

        else {
            Log.d(LOG_TAG, "playbackStatus(): SSPlayerFragment was null.");
        }
    }

    // setDuration(): Signals the SSPlayerFragment to set the max duration for the Spotify song.
    private void setDuration(int duration) {

        Log.d(LOG_TAG, "setDuration(): Duration of the current song: " + duration);

        if (playerFragment != null) {
            try { ((OnMusicPlayerListener) playerFragment).setDuration(duration); }
            catch (ClassCastException cce) {} // Catch for class cast exception errors.
        }

        else {
            Log.d(LOG_TAG, "setDuration(): SSPlayerFragment was null.");
        }
    }
}