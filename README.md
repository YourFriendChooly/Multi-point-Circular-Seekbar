# Multi-point-Circular-Seekbar


<h2>Multi-point Circular Seekbar</h2>

This is a Modification of <a href = "https://github.com/devadvance/circularseekbar"> Matt Joseph's Circular Seekbar </a>
with the added functionality of multiple Thumbs.

<h2> Documentation </h2>
In modifying Matt's work for multiple thumbs, each thumb must now be instantiated as a separate object using the following method calls:

    Circularseekbar.addPointer(int pProgress)
    Instantiates a pointer at pProgress (measured in percentage)

    Circularseekbar.addPointer(int pProgress, OnCircularSeekBarChangeListener l)
    Instantiates a pointer at pProgress using a seekbarchange listener (use new CircularSeekBar.OnCircularSeekBarChangeListener()
    to receive independant callbacks for each thumb currently selected)

    Circularseekbar.addPointer()
    No argument constructer will evenly distribute the pointers across the entirety of the circle.
    
    Circularseekbar.addPointer(OnCircularSeekBarChangeListener l)
    Same as above, with the ability to add an independant listener.

Additionally, each thumb can have it's own color defined using the builder pattern:

    Circularseekbar.addPointer().setColor(int Color);
    
<h2> Listeners </h2>

    onProgressChanged(CircularSeekBar circularSeekBar, int absoloutePprogress, float relativeProgress, Pointer pointer, boolean fromUser);
absoloutepProgress is the progress (in percentage) relative to the Circular Seekbar's origin,<br>
relativeProgress is the progress (in percentage) relative to the Pointer nearest right-most neighbor.<br>
pointer will return the currently selected pointer.<br>

		public abstract void onStopTrackingTouch(CircularSeekBar circularSeekBar, Pointer pointer);

		public abstract void onStartTrackingTouch(CircularSeekBar seekBar, Pointer pointer);

<h3>This is the code of an as-yet very green android developer, and as such is far from perfect. Any advice will be graciously accepted!</h3>


