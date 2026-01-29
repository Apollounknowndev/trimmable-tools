import os
import requests
import json

# Per-mod: Update this for each mod!!!

MOD_ID = "trimmable-tools"
MOD_VERSION = "2.1.0"
CHANGELOG = """
- Added native compatibility with Enderscape. You can now trim your tools with the Stasis pattern and get a texture unique from all vanilla patterns.
  - You'll find diamond tools with the Stasis trim in End City vaults, if you're lucky!
- Reduced the number of errors thrown in the log file when unsupported trim patterns exist by ~98%.
  - The number of errors for an unsupported trim pattern by default is now 5, not 250.
- Missing trim textures now result in the tool looking as if it has no trim, rather than being the magenta and black error cube.
- Removed the config file. Defining patterns, materials and tool types the game should generate textures for is now **fully resource-pack driven**.
  - This means it is now possible for mods to implement compatibility with their custom tool tiers and tool types on their own, without needing the user to modify their Trimmable Tools config file.
  - A guide on the Github is available [here](https://github.com/Apollounknowndev/trimmable-tools/wiki/Adding-Mod-Compatibility), with a Misode generator for the file on the page."""
UPLOAD_VERSIONS = [
    #("fabric", "1.21.1"),
    #("neoforge", "1.21.1"),
    ("fabric", "1.21.11"),
    ("neoforge", "1.21.11"),
]

DEPENDENCIES = [
    {
        "name": "Datapatched",

        "project_id": "7XXwJbHD",
        "dependency_type": "required",

        "modId": 1366720,
        "relationType": 3,
    }
]

MODRINTH_ID = "MJu3fF3K"
CURSEFORGE_ID = "834329"

# Global: Should usually not be touched!

BASE_FOLDER = os.path.dirname(os.path.abspath(__file__))


MODRINTH_TOKEN = os.getenv('TOKEN_MR')
if not MODRINTH_TOKEN:
    raise EnvironmentError("MODRINTH_TOKEN is unset!")

CURSEFORGE_TOKEN = os.getenv('TOKEN_CF')
if not CURSEFORGE_TOKEN:
    raise EnvironmentError("CURSEFORGE_TOKEN is unset!")
CURSEFORGE_URL = f"https://minecraft.curseforge.com/api/v1/projects/{CURSEFORGE_ID}/upload-file"
CURSEFORGE_GAME_VERSIONS = {
    "1.21.1": [11779],
    "1.21.11": [14406],
}
CURSEFORGE_LOADERS = {
    "fabric": 7499,
    "forge": 7498,
    "neoforge": 10150,
}


# Code

def upload_modrinth(loader: str, version: str, file_path: str):
    metadata = {
        "name": f"v{MOD_VERSION} ~ {loader.title()} {version}",
        "version_number": MOD_VERSION,
        "project_id": MODRINTH_ID,
        "game_versions": [version],
        "loaders": [loader],
        "featured": True,
        "changelog": CHANGELOG,
        "version_type": "release",
        "file_parts": ["file"],
        "dependencies": DEPENDENCIES
    }

    with open(file_path, 'rb') as mod_file:
        response = requests.post(
            "https://api.modrinth.com/v2/version",
            headers={
                "Authorization": MODRINTH_TOKEN
            },
            files={
                'file': (os.path.basename(file_path), mod_file, 'application/java-archive'),
            },
            data={
                'data': json.dumps(metadata)
            }
        )

        name = f"MR {loader.title()} {version}: "

        if response.status_code == 200:
            print(name + "Success")
            print(response.json())
        else:
            print(name + f"Failed ({response.status_code})")
            print(response.text)


def upload_curseforge(loader: str, version: str, file_path: str):
    headers = {
        "X-Api-Token": CURSEFORGE_TOKEN
    }

    # Lookup version and loader IDs
    game_version_ids = CURSEFORGE_GAME_VERSIONS.get(version)
    modloader_id = CURSEFORGE_LOADERS.get(loader)

    if not game_version_ids or not modloader_id:
        print(f"Skipping CurseForge upload for {loader} {version}: unknown IDs")
        return

    # Metadata
    metadata = {
        "displayName": f"v{MOD_VERSION} ~ {loader.title()} {version}",
        "gameVersions": game_version_ids + [modloader_id],
        "releaseType": "release",
        "changelog": CHANGELOG,
        "changelogType": "markdown",
        "dependencies": DEPENDENCIES
    }
    metastr = json.dumps(metadata)

    with open(file_path, "rb") as mod_file:
        files = {
            "file": (os.path.basename(file_path), mod_file, "application/java-archive")
        }

        response = requests.post(
            f"https://minecraft.curseforge.com/api/projects/{CURSEFORGE_ID}/upload-file",
            headers=headers,
            files=files,
            data={"metadata": metastr},
            auth=("Apollo", CURSEFORGE_TOKEN)
        )

        name = f"CF {loader.title()} {version}: "

        if response.status_code == 200:
            print(name + "Success")
            print(response.json())
        else:
            print(name + f"Failed ({response.status_code})")
            print(response.text)


for modloader, game_version in UPLOAD_VERSIONS:
    mod_path = os.path.join(
        BASE_FOLDER,
        'build',
        'libs',
        f'{MOD_ID}-{MOD_VERSION}-{modloader}-{game_version}.jar'
    )

    if not os.path.exists(mod_path):
        print(f"File not found, skipping: {mod_path}")
        continue

    upload_modrinth(modloader, game_version, mod_path)
    upload_curseforge(modloader, game_version, mod_path)

input("Press any button to close.")