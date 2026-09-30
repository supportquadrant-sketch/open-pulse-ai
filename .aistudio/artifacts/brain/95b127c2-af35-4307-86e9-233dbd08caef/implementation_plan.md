# OpenPulse AI Image Creation & Editing Engine

High-speed text-to-image synthesis and prompt-driven image editing powered by `gemini-2.5-flash-image`, integrated seamlessly into the conversational chat experience with attachment picking, style presets, and instant visual previews.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following technical and UX choices were confirmed during Phase 1 clarification:
> - **In-Chat Integration**: Image creation actions, attachment pickers, and style presets live directly within the chat composer and message stream.
> - **Dual Workflow (Generation + Editing)**: Users can create new images from scratch via prompts AND edit existing generated images or device photos using natural language instructions (e.g. "make the lighting dramatic", "replace the background with cyberpunk Tokyo").
> - **Creative Style Presets**: Fast 1-tap style chips (Photorealistic, 3D Render, Anime/Digital Art, Cyberpunk, Watercolor Sketch) that enhance prompts for high-impact outputs.
> - **Engine Optimization**: Uses `gemini-2.5-flash-image` with `responseModalities: ["TEXT", "IMAGE"]` tailored specifically for sub-second/rapid generation and high-volume throughput.

---

## 1. Overview & Core Concept

- **What It Does**: Adds multimodal image intelligence to OpenPulse AI. Users can generate vivid artwork, mockups, and illustrations from text prompts, or attach a photo / select a previously generated image to edit it with conversational instructions.
- **Target Audience / Persona**: Creators, designers, students, and mobile power users who require fast, high-volume image creation and seamless conversational editing on Android.
- **Key Value**: Low latency image generation without context switching to third-party tools, complete with persistent local caching and full-screen inspection.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Text-to-Image Creation**:
   - User taps the new **`+ / Image (Palette)`** action chip in the bottom composer.
   - An intuitive **Image Creator Bar** expands above the input field with style preset chips (`Photorealistic`, `3D Render`, `Anime`, `Cyberpunk`, `Watercolor`) and aspect ratio options.
   - User types an image prompt (e.g. "A glowing crystal lotus floating on calm neon waters") and hits Send.
   - AI generates the image rapidly using `gemini-2.5-flash-image`, rendering an interactive image bubble with download, share, full-screen zoom, and **"Edit with AI"** buttons.

2. **Prompt-Guided Image Editing**:
   - User selects **"Edit"** on any generated image in chat, OR taps the **Photo Picker** icon to upload a photo from their device gallery.
   - A thumbnail preview badge docks in the composer indicating active image-edit mode (with a 1-tap clear button).
   - User enters instruction (e.g. "Add a retro sci-fi helmet" or "Convert to vintage oil painting").
   - AI outputs the edited image alongside the original with a before/after split viewer affordance.

3. **Full-Screen Lightbox & Actions**:
   - Tapping any image opens an immersive fullscreen viewer with pinch-to-zoom, save to device gallery, copy to clipboard, and 1-tap remix prompt.

### Visual Identity & Theme
- **Atmosphere**: Consistent with OpenPulse dark ethereal glassmorphism (`#0A0F1D` background with `FogSphereBackground` swirling mist).
- **Style Chips**: Pill-shaped horizontal scroll container with luminous cyan/indigo borders and glowing active state indicators.
- **Image Cards**: Rounded 16.dp corners, subtle elevated border, shimmer skeleton loader during generation, and token/generation latency badge.

---

## 3. Key Product Decisions & Trade-Offs

- **Model Choice (`gemini-2.5-flash-image`)**:
  - *Chosen Approach*: Specifically utilizes `gemini-2.5-flash-image` as required by the brief for speed and high-volume use cases.
  - *Why*: Delivers rapid time-to-first-image, minimal overhead, and full support for both multimodal text-to-image and image+text editing.
  - *Fallback*: Gracefully handles quota with automatic retry and clear feedback.

- **Storage & Offline Caching**:
  - *Chosen Approach*: Persist generated image byte arrays into app-internal storage (`filesDir/images/`), storing the local file URI in Room `MessageEntity`.
  - *Why*: Prevents storing megabyte-sized Base64 blobs inside SQLite (avoiding `CursorWindow` 2MB limits) while enabling instantaneous offline message loading.

- **Zero-Permission Photo Picker**:
  - *Chosen Approach*: Modern Android `ActivityResultContracts.PickVisualMedia()` for zero-permission photo selection, complying strictly with Google Play policies.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          ChatScreen (Compose)                          │
│  ┌───────────────────────┐  ┌────────────────────────────────────────┐ │
│  │    Message Bubble     │  │          Composer Bar                  │ │
│  │ ┌───────────────────┐ │  │ ┌────────────────────────────────────┐ │ │
│  │ │ Generated Image   │ │  │ │ [Style Chips] [Aspect Ratio Pills] │ │ │
│  │ │ [Zoom] [Save]     │ │  │ └────────────────────────────────────┘ │ │
│  │ │ [Edit via Prompt] │ │  │ [Attachment Badge] [Text Input] [Send] │ │
│  │ └───────────────────┘ │  └────────────────────────────────────────┘ │
│  └───────────────────────┘                                             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             ChatViewModel                              │
│  - onSendImagePrompt(prompt, style, attachedBitmap?)                   │
│  - onSelectImageForEdit(imageUri)                                      │
│  - cancelAttachedImage()                                               │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             ImageRepository                            │
│  - generateImage(prompt, style, aspect, inputBitmap?, apiKey)          │
│  - saveImageToInternalStorage(context, bytes) -> Uri                   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Gemini REST API Client                          │
│  POST v1beta/models/gemini-2.5-flash-image:generateContent             │
│  - responseModalities: ["TEXT", "IMAGE"]                               │
│  - inlineData: { mimeType, data: base64 } (when editing)               │
└────────────────────────────────────────────────────────────────────────┘
```

### Database Updates
- Add `imageUri: String?` and `inputImageUri: String?` to `MessageEntity` in Room.
- Update `AppDatabase` version and schema migration.

### Image Generation & Editing Request Payload
```json
{
  "contents": [
    {
      "parts": [
        { "text": "[Style: Photorealistic] High dynamic range cinematic portrait..." },
        { "inlineData": { "mimeType": "image/jpeg", "data": "<base64>" } }
      ]
    }
  ],
  "generationConfig": {
    "responseModalities": ["TEXT", "IMAGE"],
    "imageConfig": {
      "aspectRatio": "1:1"
    }
  }
}
```
