#!/usr/bin/env python3
"""Regenerate the dependency-free Xcode project with stable object identifiers."""
import hashlib, json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
objects = {}
def ident(name): return hashlib.sha1(name.encode()).hexdigest()[:24].upper()
def add(object_name, isa, **fields):
    key = ident(object_name); objects[key] = dict(isa=isa, **fields); return key
class Ref(str): pass
def ref(key): return Ref(key)
def fmt(value):
    if isinstance(value, Ref): return value
    if isinstance(value, dict): return '{ ' + ' '.join(k + ' = ' + fmt(v) + ';' for k,v in value.items()) + ' }'
    if isinstance(value, list): return '(' + ', '.join(fmt(v) for v in value) + ')'
    return json.dumps(str(value))
package = add('package', 'XCLocalSwiftPackageReference', relativePath='ZotMacrosCore')
products = []
targets = []
main_children = []
app_id = ident('target:ZotMacros')
for name, folder, product_type in [('ZotMacros','ZotMacros','application'),('ZotMacrosTests','ZotMacrosTests','bundle.unit-test'),('ZotMacrosUITests','ZotMacrosUITests','bundle.ui-testing'),('ZotMacrosUITestHost','ZotMacrosUITestHost','application')]:
    source_refs, source_builds, resource_builds = [], [], []
    paths = list((root/folder).rglob('*.swift'))
    is_app = product_type == 'application'
    if name == 'ZotMacrosUITestHost': paths += [p for p in (root/'ZotMacros').rglob('*.swift') if p.name != 'ZotMacrosApp.swift']
    for path in sorted(paths):
        relative = str(path.relative_to(root))
        f = add(name+':file:'+relative, 'PBXFileReference', lastKnownFileType='sourcecode.swift', path=relative, sourceTree='<group>')
        source_refs.append(ref(f)); source_builds.append(ref(add(name+':build:'+relative, 'PBXBuildFile', fileRef=ref(f))))
    if is_app:
        for path, kind in [('ZotMacros/Assets.xcassets', 'folder.assetcatalog'),('ZotMacros/PrivacyInfo.xcprivacy','text.xml')]:
            f = add(name+':file:'+path, 'PBXFileReference', lastKnownFileType=kind, path=path, sourceTree='<group>'); source_refs.append(ref(f)); resource_builds.append(ref(add(name+':build:'+path, 'PBXBuildFile', fileRef=ref(f))))
    group = add('group:'+name, 'PBXGroup', children=source_refs, name=name, sourceTree='<group>'); main_children.append(ref(group))
    product = add('product:'+name, 'PBXFileReference', explicitFileType='wrapper.application' if is_app else 'wrapper.cfbundle', path=name+('.app' if is_app else '.xctest'), sourceTree='BUILT_PRODUCTS_DIR'); products.append(ref(product))
    dependencies, framework_builds, package_products = [], [], []
    if name != 'ZotMacrosUITests':
        for module in ['ZotMacrosCore','ZotMacrosStorage','ZotMacrosState']:
            dep = add(name+':dep:'+module,'XCSwiftPackageProductDependency',package=ref(package),productName=module); package_products.append(ref(dep)); framework_builds.append(ref(add(name+':link:'+module,'PBXBuildFile',productRef=ref(dep))))
    if not is_app:
        dependency_id = ident('target:ZotMacrosUITestHost') if name == 'ZotMacrosUITests' else app_id
        proxy = add(name+':proxy','PBXContainerItemProxy',containerPortal=ref(ident('project')),proxyType='1',remoteGlobalIDString=ref(dependency_id),remoteInfo='ZotMacrosUITestHost' if name == 'ZotMacrosUITests' else 'ZotMacros')
        dependencies.append(ref(add(name+':dependency','PBXTargetDependency',target=ref(dependency_id),targetProxy=ref(proxy))))
    phases = [ref(add(name+':sources','PBXSourcesBuildPhase',buildActionMask='2147483647',files=source_builds,runOnlyForDeploymentPostprocessing='0')), ref(add(name+':frameworks','PBXFrameworksBuildPhase',buildActionMask='2147483647',files=framework_builds,runOnlyForDeploymentPostprocessing='0')), ref(add(name+':resources','PBXResourcesBuildPhase',buildActionMask='2147483647',files=resource_builds,runOnlyForDeploymentPostprocessing='0'))]
    configs=[]
    for mode in ['Debug','Release']:
        settings=dict(PRODUCT_NAME='$(TARGET_NAME)',PRODUCT_BUNDLE_IDENTIFIER='com.zotmacros.ios'+('' if name=='ZotMacros' else '.'+name),SWIFT_VERSION='5.0',IPHONEOS_DEPLOYMENT_TARGET='17.0',TARGETED_DEVICE_FAMILY='1',GENERATE_INFOPLIST_FILE='YES',CODE_SIGN_STYLE='Automatic',SUPPORTED_PLATFORMS='iphoneos iphonesimulator',SDKROOT='iphoneos',SWIFT_EMIT_LOC_STRINGS='YES',LD_RUNPATH_SEARCH_PATHS=['$(inherited)','@executable_path/Frameworks','@loader_path/Frameworks'])
        if is_app: settings.update(ASSETCATALOG_COMPILER_APPICON_NAME='AppIcon',MARKETING_VERSION='1.0',CURRENT_PROJECT_VERSION='1',INFOPLIST_KEY_CFBundleDisplayName='ZotMacros',INFOPLIST_KEY_UILaunchScreen_Generation='YES',INFOPLIST_KEY_UIApplicationSceneManifest_Generation='YES',INFOPLIST_KEY_UISupportedInterfaceOrientations='UIInterfaceOrientationPortrait UIInterfaceOrientationLandscapeLeft UIInterfaceOrientationLandscapeRight')
        elif name=='ZotMacrosTests': settings.update(TEST_HOST='$(BUILT_PRODUCTS_DIR)/ZotMacros.app/ZotMacros',BUNDLE_LOADER='$(TEST_HOST)')
        else: settings.update(TEST_TARGET_NAME='ZotMacrosUITestHost')
        configs.append(ref(add(name+':'+mode,'XCBuildConfiguration',buildSettings=settings,name=mode)))
    config_list=add(name+':config','XCConfigurationList',buildConfigurations=configs,defaultConfigurationIsVisible='0',defaultConfigurationName='Release')
    target=add('target:'+name,'PBXNativeTarget',buildConfigurationList=ref(config_list),buildPhases=phases,buildRules=[],dependencies=dependencies,name=name,packageProductDependencies=package_products,productName=name,productReference=ref(product),productType='com.apple.product-type.'+product_type); targets.append(ref(target))
product_group=add('products','PBXGroup',children=products,name='Products',sourceTree='<group>');main_children.append(ref(product_group))
main=add('main','PBXGroup',children=main_children,sourceTree='<group>')
configs=[]
for mode in ['Debug','Release']:
    settings=dict(ONLY_ACTIVE_ARCH='YES' if mode=='Debug' else 'NO',CLANG_ENABLE_MODULES='YES',CLANG_ENABLE_OBJC_ARC='YES',SWIFT_OPTIMIZATION_LEVEL='-Onone' if mode=='Debug' else '-O',DEBUG_INFORMATION_FORMAT='dwarf' if mode=='Debug' else 'dwarf-with-dsym',ENABLE_TESTABILITY='YES' if mode=='Debug' else 'NO',SWIFT_ACTIVE_COMPILATION_CONDITIONS='DEBUG' if mode=='Debug' else '',SWIFT_STRICT_CONCURRENCY='targeted')
    configs.append(ref(add('project:'+mode,'XCBuildConfiguration',name=mode,buildSettings=settings)))
config=add('project:config','XCConfigurationList',buildConfigurations=configs,defaultConfigurationIsVisible='0',defaultConfigurationName='Release')
project=add('project','PBXProject',attributes=dict(LastUpgradeCheck='1600',BuildIndependentTargetsInParallel='YES'),buildConfigurationList=ref(config),compatibilityVersion='Xcode 14.0',developmentRegion='en',hasScannedForEncodings='0',knownRegions=['en','Base'],mainGroup=ref(main),productRefGroup=ref(product_group),projectDirPath='',projectRoot='',packageReferences=[ref(package)],targets=targets)
folder=root/'ZotMacros.xcodeproj';folder.mkdir(exist_ok=True)
(folder/'project.pbxproj').write_text('// !$*UTF8*$!\n'+fmt(dict(archiveVersion='1',classes={},objectVersion='56',objects=objects,rootObject=ref(project)))+'\n')
scheme=folder/'xcshareddata/xcschemes';scheme.mkdir(parents=True,exist_ok=True)
def buildable(name): return f'<BuildableReference BuildableIdentifier="primary" BlueprintIdentifier="{ident("target:"+name)}" BuildableName="{name}{".app" if name=="ZotMacros" else ".xctest"}" BlueprintName="{name}" ReferencedContainer="container:ZotMacros.xcodeproj"/>'
(scheme/'ZotMacros.xcscheme').write_text(f'''<?xml version="1.0" encoding="UTF-8"?>
<Scheme LastUpgradeVersion="1600" version="1.3">
<BuildAction parallelizeBuildables="YES" buildImplicitDependencies="YES"><BuildActionEntries><BuildActionEntry buildForTesting="YES" buildForRunning="YES" buildForProfiling="YES" buildForArchiving="YES" buildForAnalyzing="YES">{buildable('ZotMacros')}</BuildActionEntry></BuildActionEntries></BuildAction>
<TestAction buildConfiguration="Debug" selectedDebuggerIdentifier="Xcode.DebuggerFoundation.Debugger.LLDB" selectedLauncherIdentifier="Xcode.IDEFoundation.Launcher.LLDB" shouldUseLaunchSchemeArgsEnv="YES"><Testables><TestableReference skipped="NO">{buildable('ZotMacrosTests')}</TestableReference><TestableReference skipped="NO">{buildable('ZotMacrosUITests')}</TestableReference></Testables></TestAction>
<LaunchAction buildConfiguration="Debug" selectedDebuggerIdentifier="Xcode.DebuggerFoundation.Debugger.LLDB" selectedLauncherIdentifier="Xcode.IDEFoundation.Launcher.LLDB" launchStyle="0" useCustomWorkingDirectory="NO" ignoresPersistentStateOnLaunch="NO" debugDocumentVersioning="YES" allowLocationSimulation="YES"><BuildableProductRunnable runnableDebuggingMode="0">{buildable('ZotMacros')}</BuildableProductRunnable></LaunchAction>
<ProfileAction buildConfiguration="Release" shouldUseLaunchSchemeArgsEnv="YES" savedToolIdentifier="" useCustomWorkingDirectory="NO" debugDocumentVersioning="YES"><BuildableProductRunnable runnableDebuggingMode="0">{buildable('ZotMacros')}</BuildableProductRunnable></ProfileAction>
<AnalyzeAction buildConfiguration="Debug"/><ArchiveAction buildConfiguration="Release" revealArchiveInOrganizer="YES"/>
</Scheme>''')
print('Generated ZotMacros.xcodeproj with app, unit tests and UI tests.')
