## Custom Supplemental Demo Agent

Demo agent for testing how Tink Link (or other SDKs, python scripts, etc) will display supplemental fields.

## Usage

This agent expects a fake credential with just one field filled, `fieldsInJson`, with a json value, representing one of: 
 * A single Field
 * A list of Fields
 * A list of lists of Fields
 
Agent then parses the provided json, and asks user for fake supplemental information, displaying provided fields on one or more screens.

Can be used by running appropriate AgentTest, or Staging Tink Demo (check providers, should be available on DE market)
