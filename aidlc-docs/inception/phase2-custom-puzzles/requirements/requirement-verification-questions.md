# Requirements Clarification Questions — Phase 2: Custom Photo Puzzles

Answer each after the `[Answer]:` tag; choose "Other" and describe if none fit.

**Already locked (not re-asked):** CameraX for capture; `PickVisualMedia` system
photo picker (no storage permission); CAMERA permission requested only at
capture; images processed on-device and stored in `filesDir/puzzles/` with a Room
row (schema already has `ImageRef.FileRef`); photos never leave the device;
offline; adjacent-only swap gameplay reused; Hilt/StateFlow architecture reused.
The extension config (Security/Resiliency/PBT enabled) carries over from Phase 1.

## Question 1: Entry points for creating a custom puzzle
The Home "Create from camera" CTA currently shows "Coming soon". What creation
entry should Phase 2 wire up?

A) Both camera capture AND "choose from photos" — the CTA opens a small chooser (Take photo / Choose photo)

B) Camera capture only (photo picker deferred to a later phase)

C) Photo picker only (camera deferred)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 2: Crop / framing before generating
Puzzles are square. How should the user's photo become square?

A) Auto center-crop to square (no manual crop UI) — simplest; the Review screen shows the square result to accept/retake

B) Provide a manual crop/reposition step (pinch-zoom + pan to choose the square region) before generating

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 3: Difficulty choice for a custom puzzle
After accepting the photo, how is difficulty chosen?

A) Pick size once at creation (Easy/Medium/Hard) → generate → play that size; the saved puzzle can later be replayed at any size from Puzzle Select

B) Don't pick at creation — just save the photo; always choose difficulty later when playing (like bundled puzzles)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 4: Where saved custom puzzles appear
Custom puzzles are saved to the library ("My puzzles"). How do they surface for play?

A) In a dedicated "My puzzles" library screen (grid of saved custom puzzles) with its own play/delete; also shown in Puzzle Select alongside bundled ones

B) Only in the "My puzzles" library screen (kept separate from the bundled Puzzle Select lists)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 5: Naming a custom puzzle
Saved puzzles need a name (for the library + Continue card).

A) Auto-name them ("My Puzzle 1", "My Puzzle 2", … or by date) — no typing required

B) Prompt the user to type a name at save time (with an auto default they can keep)

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 6: Image processing target resolution
To avoid OOM and keep tiles crisp, the accepted photo is downsampled before
slicing. What target?

A) ~1024 px square source (matches bundled puzzles; small files, crisp enough for 5×5) — recommended

B) ~2048 px square source (sharper tiles, larger files/memory)

C) Match device screen resolution dynamically

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 7: Delete behavior
Deleting a custom puzzle (already designed in Phase 1: removes Room row + image
files). What confirmation UX?

A) Confirmation sheet/dialog before delete ("Delete this puzzle? This can't be undone.") — recommended

B) Immediate delete with an Undo snackbar

X) Other (please describe after [Answer]: tag below)

[Answer]:

## Question 8: Camera permission denial handling
If the user denies the camera permission, what should happen?

A) Show a friendly "Camera access needed" screen explaining why, with a button to open app Settings; offer "Choose from photos" as an alternative if the picker is enabled (Q1)

B) Just fall back silently to the photo picker (if enabled), no explanation screen

X) Other (please describe after [Answer]: tag below)

[Answer]:
