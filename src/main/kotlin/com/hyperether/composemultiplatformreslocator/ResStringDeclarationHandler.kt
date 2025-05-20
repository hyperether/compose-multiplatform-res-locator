package com.hyperether.composemultiplatformreslocator

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.getQualifiedExpressionForSelector

class ResStringGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor
    ): Array<PsiElement>? {
        val project = sourceElement?.project ?: return null

        val resourcePair = findResourceKey(sourceElement) ?: return null
        val resourceKey = resourcePair.first

        val result = mutableListOf<PsiElement>()

        // Search for all strings.xml files in the project
        FileTypeIndex.getFiles(
            com.intellij.openapi.fileTypes.StdFileTypes.XML,
            GlobalSearchScope.projectScope(project)
        ).forEach { virtualFile ->
            // Look for files named strings.xml in values* directories
            if (isStringsXmlFile(virtualFile)) {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile) as? XmlFile
                    ?: return@forEach

                // Find matching string resource
                val stringElement = findStringElementByName(psiFile, resourceKey)
                if (stringElement != null) {
                    result.add(stringElement)
                }
            } else if (isDrawableFile(virtualFile)) {
                val fileName = virtualFile.nameWithoutExtension
                if (fileName == resourceKey) {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    psiFile?.firstChild?.let {
                        result.add(it)
                    }

                }

                if (result.isEmpty()) {
                    // Search for common image file types
                    val imageExtensions = listOf("png", "jpg", "jpeg", "gif", "webp", "9.png")
                    for (extension in imageExtensions) {
                        val fileName = "$resourceKey.$extension"
                        FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.projectScope(project))
                            .forEach { virtualFile ->
                                if (isDrawableFile(virtualFile)) {
                                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                                    psiFile?.let {
                                        result.add(it)
                                    }
                                }
                            }
                    }
                }
            }
        }

        return if (result.isNotEmpty()) result.toTypedArray() else null
    }

    private fun findResourceKey(element: PsiElement?): Pair<String?, Type?>? {
        if (element == null) return null

        var current = element

        if (element is KtNameReferenceExpression) {
            val qualifiedExpression = element.getQualifiedExpressionForSelector()
            if (qualifiedExpression != null) {
                return extractResourceKeyFromExpression(qualifiedExpression)
            }
        }

        var dotQualifier: KtDotQualifiedExpression? = null
        while (current != null) {
            if (current is KtDotQualifiedExpression) {
                dotQualifier = current
                break
            }
            current = current.parent
        }

        return dotQualifier?.let { extractResourceKeyFromExpression(it) }
    }

    private fun extractResourceKeyFromExpression(expression: KtExpression): Pair<String?, Type>? {
        val fullExpression = expression.text

        if (fullExpression.startsWith("Res.string.") || fullExpression.startsWith("Res.strings.")) {
            val resourceKey = fullExpression.substringAfterLast('.')
            return if (resourceKey.isNotEmpty() && resourceKey.matches("[a-zA-Z_][a-zA-Z0-9_]*".toRegex())) {
                Pair(resourceKey, Type.STRING)
            } else null
        } else if (fullExpression.startsWith("Res.drawable.")) {
            val resourceKey = fullExpression.substringAfterLast('.')
            return if (resourceKey.isNotEmpty() && resourceKey.matches("[a-zA-Z_][a-zA-Z0-9_]*".toRegex())) {
                Pair(resourceKey, Type.DRAWABLE)
            } else null
        }

        return null
    }

    private fun isStringsXmlFile(virtualFile: VirtualFile): Boolean {
        if (virtualFile.name != "strings.xml") return false

        // Check if it's in a values* directory
        val parent = virtualFile.parent
        return parent?.name?.startsWith("values") == true
    }

    private fun isDrawableFile(virtualFile: VirtualFile): Boolean {
        val parent = virtualFile.parent
        if (virtualFile.path.contains("build")) {
            return false
        }
        return parent?.name?.startsWith("drawable") == true
    }

    private fun findStringElementByName(xmlFile: XmlFile, name: String?): PsiElement? {
        val rootTag = xmlFile.rootTag ?: return null

        // Look for <string name="key">value</string>
        val stringTags = rootTag.findSubTags("string")
        for (tag in stringTags) {
            if (tag.getAttributeValue("name") == name) {
                // Return the opening tag, which includes the name attribute
                return tag.getAttribute("name")?.valueElement ?: tag
            }
        }

        return null
    }

    override fun getActionText(context: DataContext): String? {
        return "Go to XML Resource"
    }
}

private enum class Type {
    STRING,
    DRAWABLE
}