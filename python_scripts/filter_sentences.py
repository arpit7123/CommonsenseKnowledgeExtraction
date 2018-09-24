

import re
import os


if __name__=="__main__":
    data_dir = "./data/"
    for f in os.listdir(data_dir):
        file_path = os.path.join(data_dir,f)
        
        f = open(file_path,'r')
        for line in f:
            #matchObj = re.match( r'(.*) because (.*?) .*', line, re.M|re.I)
            match_obj = re.match(r'(.*)(lead to|leads to|led to|leading to|give rise to|gave rise to|given rise to|giving rise to|induce|inducing|induced|induces|cause|causes|causing|caused|caused by|bring on|brings on|brought on|bringing on|result from|resulting from|results from|resulted from|, because|because|because of|, thus|, therefore|, inasmuch as|due to|in consequence of|owing to|as a result of|and hence|as a consequence of|, hence|, consequently|and consequently|, for this reason alone ,)(.*)',line,re.M|re.I)
            if match_obj:
                print line.strip()
        
