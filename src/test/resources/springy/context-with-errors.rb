
# context config with errors in it
#

bean "bean1", "springy.beans.Bean6", :init_method=>"initialise" do |b|
    b.bean2 = :bean2
    b.listProperty = ["1","2","3"]
    b.initialiser do |instance|
        instance.do_something(22)
    end
end