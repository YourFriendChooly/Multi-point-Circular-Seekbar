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

Additionally, each thumb can have it's own color defined using the builder pattern:

    Circularseekbar.addPointer().setColor(int Color);

<h3>This is the code of an as-yet very green android developer, and as such is far from perfect. Any advice will be graciously accepted!</h3>


