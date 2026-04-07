chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
        id: "translate-darija",
        title: "Translate to Darija",
        contexts: ["selection"]
    });

    chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true })
        .catch((error) => console.error("setPanelBehavior error:", error));
});

chrome.contextMenus.onClicked.addListener((info) => {
    if (info.menuItemId !== "translate-darija") return;

    const selectedText = info.selectionText || "";

    chrome.storage.local.set({ selectedText })
        .catch((error) => console.error("Storage error:", error));
});