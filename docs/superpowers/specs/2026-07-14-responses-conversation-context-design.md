# Responses Conversation Context Design

## Goal

Make every request after the first succeed while preserving the complete, ordered history of the active local conversation.

## Scope

`ChatViewModel` continues to load persisted messages for one `conversationId`, order them by `createdAt`, and pass the resulting `ChatMessage` list to `ResponsesClient`. No conversation storage, selection, or API endpoint changes are required.

## Request Encoding

`ResponsesClient` will build a `/responses` `input` array from every message in the active conversation:

```json
[
  {"role":"user","content":[{"type":"input_text","text":"First question"}]},
  {"role":"assistant","content":[{"type":"output_text","text":"First answer"}]},
  {"role":"user","content":[{"type":"input_text","text":"Follow-up question"}]}
]
```

User messages use `input_text`. Assistant messages use `output_text`. The currently sent image remains attached only to the final user message as `input_image`.

## Error Handling

HTTP failures continue to show the status code and a bounded response body in the existing UI error state. The fix changes request validity; it does not suppress or retry server errors.

## Tests

Add a JVM test for a user-assistant-user history. It must assert that all three messages are retained in chronological order and that the assistant entry uses `output_text`, preventing the second-message 502 regression.
