## maintainer stuff

namespace :maintainer do
    WWWROOT             = "/var/www/code.trampolinesystems.com"
    SVNROOT             = "http://svn.trampolinesystems.com/springy/trunk"
    SVERSION            = "0.3"
    DISTBUILDDIR        = "/tmp"
    TMPDIR              = File.join(DISTBUILDDIR, "springy-#{SVERSION}")
    BALLSDIR            = File.join(BUILD_DIR, "balls")

    directory BALLSDIR

    task :publish_doc  do |t|
      sh "scp -r rdoc/* trampolinesystems.com:#{WWWROOT}/doc/springy/"
      sh "scp -r build/javadoc/* trampolinesystems.com:#{WWWROOT}/doc/springy/javadoc"
    end
    task :publish_doc => :doc

    task :create_tarball do |t|
        rm_rf TMPDIR
        sh "svn export #{SVNROOT} #{TMPDIR}"
        Dir.chdir(TMPDIR) do
            sh "rake dist javadoc"
            sh "rm *.ipr *.iml"
            rm_rf "externals"
            rm_rf BUILD_DIR
            rm_f __FILE__
        end

        tar = File.expand_path(File.join(BALLSDIR, "springy-#{SVERSION}.tar.bz2"), Rake.original_dir)
        zip = File.expand_path(File.join(BALLSDIR, "springy-#{SVERSION}.zip"), Rake.original_dir)

        Dir.chdir(DISTBUILDDIR) do
            sh "tar cfvj #{tar} springy-#{SVERSION}"
            sh "zip -r #{zip} springy-#{SVERSION}"
        end
    end
    task :create_tarball => [ :prepare_balls ]

    task :publish_tarball do
        sh "scp -r #{BALLSDIR}/* trampolinesystems.com:#{WWWROOT}/download/springy/"
    end
    task :publish_tarball => :create_tarball

    task :publish => [ :test, :publish_doc, :publish_tarball ]

    task :prepare_balls do
        rm_rf BALLSDIR
        mkdir_p BALLSDIR
    end
end
