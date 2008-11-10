require 'rake/clean'
require 'rake/rdoctask'

require 'jerbil/javac_task'
require 'jerbil/testng_task'
require 'jerbil/javadoc_task'
require 'jerbil/jar_task'
require 'jerbil/dependency_task'

MAIN_SOURCE_DIR     = "src/main/java"
MAIN_RESOURCES_DIR  = "src/main/resources"
TEST_SOURCE_DIR     = "src/test/java"
TEST_RESOURCES_DIR  = "src/test/resources"
BUILD_DIR           = "build"
DIST_DIR           	= "dist"
JAVA_BUILD_DIR      = "#{BUILD_DIR}/java"
JAVADOC_DIR         = "#{BUILD_DIR}/javadoc"
JAVA_FILES          = JavaFileList.new(MAIN_SOURCE_DIR, JAVA_BUILD_DIR, MAIN_RESOURCES_DIR)
JAVA_TEST_FILES     = JavaFileList.new(TEST_SOURCE_DIR, JAVA_BUILD_DIR, TEST_RESOURCES_DIR)

CLASSPATH           = FileList[ JAVA_BUILD_DIR, "./lib/*.jar" ]
DISTJAR             = File.join(Rake.original_dir, DIST_DIR, "springy.jar")
TESTOUTPUT          = File.join(Rake.original_dir, "test-output")


CLEAN.include(BUILD_DIR,DIST_DIR)
Jerbil::DependencyTask.load

load_jvm(CLASSPATH, JAVA_BUILD_DIR, :loggingprops => "logging.properties")

task :default => :compile

desc "compile all java files"
Jerbil::JavacTask.new(:compile) do |jct|
  jct.java_files = JAVA_FILES
  jct.options :debug
end

desc "compile all tests"
Jerbil::JavacTask.new(:test_compile) do |jct|
  jct.java_files = JAVA_TEST_FILES
  jct.options :debug
  jct.depends_on :compile
end

task :prepare_tests do
  rm_rf TESTOUTPUT
  rm_rf BUILD_DIR
end

desc "run tests"
Jerbil::TestNG::TestNGTask.new(:test) do |t|
  t.tests = JAVA_TEST_FILES
  t.outputdir = TESTOUTPUT
  t.depends_on :prepare_tests, :test_compile
end

Jerbil::JavaDocTask.new do |t|
  t.sourcepath = MAIN_SOURCE_DIR
  t.dstdir = JAVADOC_DIR
  t.depends_on :compile
  t.subpackages = "springy"
  [ "http://java.sun.com/j2se/1.5.0/docs/api",
    "http://static.springframework.org/spring/docs/2.0.x/api"].each do |l|
    t.link = l
  end
end

Jerbil::JarTask.new do |t|
  t.dir = JAVA_BUILD_DIR
  t.filename = DISTJAR
  t.depends_on :clean, :compile
end

Rake::RDocTask.new do |rdoc|
  rdoc.title    = "Springy"
  rdoc.options << '--line-numbers' << '--inline-source' << '--main' << 'README'
  rdoc.rdoc_files.include("README", "CHANGES", "TODO", "LICENSE", "src/**/*.rb")
  rdoc.rdoc_dir = 'rdoc'
  rdoc.template = "externals/allison/allison.rb"
end

desc "generate full documentation"
task :doc => [ :rerdoc, :javadoc ]

desc "build distribution"
task :dist => :jar

task :cruise do
  begin
    Rake::Task['test'].invoke
  ensure
    if ENV['CC_BUILD_ARTIFACTS']
      cp_r TESTOUTPUT, ENV['CC_BUILD_ARTIFACTS']
    end
  end
end

begin
    require 'maintainer'
rescue LoadError
end
